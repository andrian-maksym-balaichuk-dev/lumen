package sk.mpage.lumen.feature.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.repository.CategoryRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.ui.toUiText
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
internal class LobbyViewModel
    @Inject
    constructor(
        profiles: UserProfileRepository,
        private val categories: CategoryRepository,
        clock: Clock,
    ) : ViewModel() {
        private val selection = MutableStateFlow(LobbyUiState(greeting = Greeting.at(LocalTime.now(clock))))
        private val refresh = MutableStateFlow(RefreshState(isRunning = false))
        private var refreshJob: Job? = null

        private val effectChannel = Channel<LobbyEffect>(Channel.BUFFERED)
        val effects: Flow<LobbyEffect> = effectChannel.receiveAsFlow()

        val uiState: StateFlow<LobbyUiState> =
            combine(
                selection,
                profiles.observeProfile(),
                categories.observeCategories(),
                refresh,
            ) { state, profile, list, refreshState ->
                state.copy(
                    playerName = profile.name,
                    avatar = profile.avatar,
                    categoryId = state.categoryId?.takeIf { id -> list.isEmpty() || list.any { it.id == id } },
                    categories = categoriesState(list, refreshState),
                    isRefreshing = refreshState.isRunning && list.isNotEmpty(),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), selection.value)

        init {
            load { categories.refreshIfStale() }
        }

        fun onAction(action: LobbyAction) {
            when (action) {
                is LobbyAction.ModeSelected -> selection.update { it.copy(mode = action.mode) }
                is LobbyAction.DifficultySelected -> selection.update { it.copy(difficulty = action.difficulty) }
                is LobbyAction.TypeSelected -> selection.update { it.copy(type = action.type) }
                is LobbyAction.CategorySelected -> selection.update { it.copy(categoryId = action.categoryId) }
                LobbyAction.RefreshPulled, LobbyAction.RetryTapped -> load { categories.refresh() }
            }
        }

        private fun load(request: suspend () -> AppResult<Unit>) {
            if (refreshJob?.isActive == true) return
            refresh.value = RefreshState(isRunning = true)
            refreshJob =
                viewModelScope.launch {
                    val error = (request() as? AppResult.Failure)?.error
                    refresh.value = RefreshState(isRunning = false, error = error)
                    if (error != null && categories.observeCategories().first().isNotEmpty()) {
                        effectChannel.send(LobbyEffect.ShowMessage(error.toUiText()))
                    }
                }
        }

        private fun categoriesState(
            list: List<Category>,
            refreshState: RefreshState,
        ): CategoriesUiState =
            when {
                list.isNotEmpty() -> CategoriesUiState.Loaded(list.toImmutableList())
                refreshState.isRunning || refreshState.error == null -> CategoriesUiState.Loading
                else -> CategoriesUiState.Failed(refreshState.error)
            }

        private data class RefreshState(
            val isRunning: Boolean,
            val error: DataError? = null,
        )
    }
