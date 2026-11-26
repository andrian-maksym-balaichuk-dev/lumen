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
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import sk.mpage.lumen.core.common.result.AppResult
import sk.mpage.lumen.core.common.result.DataError
import sk.mpage.lumen.core.domain.port.NetworkMonitor
import sk.mpage.lumen.core.domain.repository.CategoryRepository
import sk.mpage.lumen.core.domain.repository.SettingsRepository
import sk.mpage.lumen.core.domain.repository.UserProfileRepository
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.model.QuizConfig
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
        network: NetworkMonitor,
        private val settings: SettingsRepository,
        clock: Clock,
    ) : ViewModel() {
        private val selection = MutableStateFlow(LobbyUiState(greeting = Greeting.at(LocalTime.now(clock))))
        private val refresh = MutableStateFlow(RefreshState(isRunning = false))
        private var refreshJob: Job? = null
        private var selectionChanged = false

        private val effectChannel = Channel<LobbyEffect>(Channel.BUFFERED)
        val effects: Flow<LobbyEffect> = effectChannel.receiveAsFlow()

        val uiState: StateFlow<LobbyUiState> =
            combine(
                selection,
                profiles.observeProfile(),
                categories.observeCategories(),
                refresh,
                network.isOnline,
            ) { state, profile, list, refreshState, isOnline ->
                state.copy(
                    playerName = profile.name,
                    avatar = profile.avatar,
                    categoryId = state.categoryId?.takeIf { id -> list.isEmpty() || list.any { it.id == id } },
                    categories = categoriesState(list, refreshState),
                    isRefreshing = refreshState.isRunning && list.isNotEmpty(),
                    isOffline = !isOnline,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), selection.value)

        init {
            restoreLastConfig()
            load { categories.refreshIfStale() }
        }

        fun onAction(action: LobbyAction) {
            when (action) {
                is LobbyAction.ModeSelected -> select { it.copy(mode = action.mode) }
                is LobbyAction.DifficultySelected -> select { it.copy(difficulty = action.difficulty) }
                is LobbyAction.TypeSelected -> select { it.copy(type = action.type) }
                is LobbyAction.CategorySelected -> select { it.copy(categoryId = action.categoryId) }
                LobbyAction.RefreshPulled, LobbyAction.RetryTapped -> load { categories.refresh() }
                LobbyAction.StartTapped -> start()
            }
        }

        private fun restoreLastConfig() {
            viewModelScope.launch {
                val saved = settings.observeSettings().first().lastConfig ?: return@launch
                if (selectionChanged) return@launch
                selection.update {
                    it.copy(
                        mode = saved.mode,
                        difficulty = saved.difficulty,
                        type = saved.type,
                        categoryId = saved.categoryId,
                    )
                }
            }
        }

        private fun select(change: (LobbyUiState) -> LobbyUiState) {
            selectionChanged = true
            val state = selection.updateAndGet(change)
            viewModelScope.launch { settings.setLastQuizConfig(config(state)) }
        }

        private fun start() {
            viewModelScope.launch {
                if (uiState.value.isOffline) return@launch
                effectChannel.send(LobbyEffect.StartGame(config(selection.value)))
            }
        }

        private suspend fun config(state: LobbyUiState): QuizConfig {
            val categoryName =
                state.categoryId?.let { id ->
                    categories
                        .observeCategories()
                        .first()
                        .firstOrNull { it.id == id }
                        ?.name
                }
            return QuizConfig(
                mode = state.mode,
                categoryId = state.categoryId.takeIf { categoryName != null },
                categoryName = categoryName.orEmpty(),
                difficulty = state.difficulty,
                type = state.type,
            )
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
