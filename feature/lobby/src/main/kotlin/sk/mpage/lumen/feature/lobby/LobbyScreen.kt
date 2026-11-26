package sk.mpage.lumen.feature.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.LumoraDockClearance
import sk.mpage.lumen.core.designsystem.component.LumoraLargeTopBar
import sk.mpage.lumen.core.designsystem.layout.LumoraFlex
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.ui.ProfileAvatar
import sk.mpage.lumen.feature.lobby.component.DifficultySelector
import sk.mpage.lumen.feature.lobby.component.ModeSelector
import sk.mpage.lumen.feature.lobby.component.TypeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LobbyScreen(
    state: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LumoraLargeTopBar(
                title = greetingText(state),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        ProfileAvatar(
                            avatar = state.avatar,
                            contentDescription = stringResource(R.string.lobby_open_profile),
                            size = Lumora.sizes.sm,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LumoraFlex(
            itemsFor = { columns ->
                val mode: @Composable () -> Unit = { ModeSection(state, onAction) }
                val difficulty: @Composable () -> Unit = { DifficultySection(state, onAction) }
                val type: @Composable () -> Unit = { TypeSection(state, onAction) }
                if (columns == 2) {
                    listOf(
                        {
                            Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xxl)) {
                                mode()
                                type()
                            }
                        },
                        difficulty,
                    )
                } else {
                    listOf(mode, difficulty, type)
                }
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(bottom = LumoraDockClearance)
                    .padding(horizontal = Lumora.spacing.gutterCompact, vertical = Lumora.spacing.lg),
        )
    }
}

@Composable
private fun ModeSection(
    state: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    ModeSelector(state.mode, onSelect = { onAction(LobbyAction.ModeSelected(it)) })
}

@Composable
private fun DifficultySection(
    state: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    DifficultySelector(state.difficulty, onSelect = { onAction(LobbyAction.DifficultySelected(it)) })
}

@Composable
private fun TypeSection(
    state: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    TypeSelector(state.type, onSelect = { onAction(LobbyAction.TypeSelected(it)) })
}

@Composable
private fun greetingText(state: LobbyUiState): String {
    if (state.playerName.isEmpty()) return ""
    val res =
        when (state.greeting) {
            Greeting.MORNING -> R.string.lobby_greeting_morning
            Greeting.AFTERNOON -> R.string.lobby_greeting_afternoon
            Greeting.EVENING -> R.string.lobby_greeting_evening
            Greeting.NIGHT -> R.string.lobby_greeting_night
        }
    return stringResource(res, state.playerName)
}

@LumoraPreviews
@Composable
private fun LobbyDefaultPreview() {
    PreviewSurface {
        LobbyScreen(
            state = LobbyUiState(greeting = Greeting.EVENING, playerName = "Ada", avatar = Avatar.Preset("owl")),
            onAction = {},
            onOpenProfile = {},
        )
    }
}

@LumoraPreviews
@Composable
private fun LobbyCustomizedPreview() {
    PreviewSurface {
        LobbyScreen(
            state =
                LobbyUiState(
                    greeting = Greeting.MORNING,
                    playerName = "Ľubomíra",
                    avatar = Avatar.Preset("fox"),
                    difficulty = Difficulty.HARD,
                    type = QuestionType.TRUE_FALSE,
                ),
            onAction = {},
            onOpenProfile = {},
        )
    }
}
