package sk.mpage.lumen.feature.awards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.LocalHazeState
import sk.mpage.lumen.core.designsystem.component.LumoraDockClearance
import sk.mpage.lumen.core.designsystem.component.LumoraLargeTopBar
import sk.mpage.lumen.core.designsystem.component.StatTile
import sk.mpage.lumen.core.designsystem.layout.LumoraFlex
import sk.mpage.lumen.core.designsystem.modifier.hazeContent
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.StreakDay
import sk.mpage.lumen.core.model.StreakDayStatus
import sk.mpage.lumen.core.model.StreakState
import sk.mpage.lumen.core.ui.StreakCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AwardsScreen(
    state: AwardsUiState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = { LumoraLargeTopBar(title = stringResource(R.string.awards_title), scrollBehavior = scrollBehavior) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (state) {
                AwardsUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is AwardsUiState.Content -> {
                    LumoraFlex(
                        items = listOf({ StreakCard(state.streak) }, { Records(state) }),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .hazeContent(LocalHazeState.current)
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    top = padding.calculateTopPadding(),
                                    start = Lumora.spacing.gutterCompact,
                                    end = Lumora.spacing.gutterCompact,
                                    bottom = padding.calculateBottomPadding() + LumoraDockClearance,
                                ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Records(state: AwardsUiState.Content) {
    Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            StatTile(
                label = stringResource(R.string.awards_marathon_best),
                value = state.marathonBest?.toString() ?: stringResource(R.string.awards_no_record),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                highlighted = state.marathonBest != null,
            )
            StatTile(
                label = stringResource(R.string.awards_best_streak),
                value = pluralStringResource(R.plurals.awards_days, state.streak.best, state.streak.best),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            StatTile(
                label = stringResource(R.string.awards_challenges_done),
                value = state.streak.challengesCompleted.toString(),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatTile(
                label = stringResource(R.string.awards_games_played),
                value = state.gamesPlayed.toString(),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@LumoraPreviews
@Composable
private fun AwardsPreview() {
    PreviewSurface {
        AwardsScreen(AwardsUiState.Content(previewStreak(), marathonBest = 24, gamesPlayed = 37))
    }
}

@LumoraPreviews
@Composable
private fun AwardsEmptyPreview() {
    PreviewSurface {
        AwardsScreen(
            AwardsUiState.Content(
                previewStreak(StreakDayStatus.MISSED).copy(current = 0, best = 0, challengesCompleted = 0),
                marathonBest = null,
                gamesPlayed = 0,
            ),
        )
    }
}

private fun previewStreak(status: StreakDayStatus? = null): StreakState {
    val today = LocalDate.of(2026, 11, 25)
    val statuses = StreakDayStatus.entries
    return StreakState(
        current = 4,
        best = 9,
        playedToday = false,
        atRisk = true,
        challengeProgress = 4,
        challengeCompleted = false,
        challengesCompleted = 2,
        lastSevenDays =
            List(7) { index ->
                StreakDay(today.minusDays(6L - index), status ?: statuses[index % statuses.size])
            },
    )
}
