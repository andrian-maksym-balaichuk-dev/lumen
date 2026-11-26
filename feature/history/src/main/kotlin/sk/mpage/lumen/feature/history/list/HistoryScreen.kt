package sk.mpage.lumen.feature.history.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import sk.mpage.lumen.core.designsystem.component.EmptyState
import sk.mpage.lumen.core.designsystem.component.LumoraDockClearance
import sk.mpage.lumen.core.designsystem.component.LumoraLargeTopBar
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.feature.history.R
import sk.mpage.lumen.feature.history.component.HistoryItem
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun HistoryScreen(
    state: HistoryUiState,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = { LumoraLargeTopBar(title = stringResource(R.string.history_title), scrollBehavior = scrollBehavior) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            when (state) {
                HistoryUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                HistoryUiState.Empty -> {
                    EmptyState(
                        icon = LumoraIcons.History,
                        title = stringResource(R.string.history_empty_title),
                        body = stringResource(R.string.history_empty_body),
                        actionLabel = stringResource(R.string.history_empty_action),
                        onAction = onStartQuiz,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is HistoryUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding =
                            PaddingValues(
                                start = Lumora.spacing.gutterCompact,
                                end = Lumora.spacing.gutterCompact,
                                bottom = padding.calculateBottomPadding() + LumoraDockClearance,
                            ),
                        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
                    ) {
                        state.items.forEach { item ->
                            when (item) {
                                is HistoryListItem.DayHeader -> {
                                    stickyHeader { DayHeader(item) }
                                }

                                is HistoryListItem.Session -> {
                                    item {
                                        HistoryItem(
                                            session = item.session,
                                            modifier =
                                                Modifier
                                                    .widthIn(
                                                        max = Lumora.spacing.contentMaxWidth,
                                                    ).fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(header: HistoryListItem.DayHeader) {
    val text =
        when (header.label) {
            DayLabel.TODAY -> stringResource(R.string.history_today)
            DayLabel.YESTERDAY -> stringResource(R.string.history_yesterday)
            DayLabel.DATE -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(header.date)
        }
    Surface(color = Lumora.glass.thick.tint, shape = MaterialTheme.shapes.small) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .padding(horizontal = Lumora.spacing.md, vertical = Lumora.spacing.xs)
                    .semantics { heading() },
        )
    }
}

@LumoraPreviews
@Composable
private fun HistoryContentPreview() {
    PreviewSurface {
        HistoryScreen(
            state =
                HistoryUiState.Content(
                    listOf(
                        HistoryListItem.DayHeader(LocalDate.of(2026, 11, 26), DayLabel.TODAY),
                        HistoryListItem.Session(previewSession(1, GameMode.CLASSIC)),
                        HistoryListItem.Session(previewSession(2, GameMode.MARATHON)),
                        HistoryListItem.DayHeader(LocalDate.of(2026, 11, 25), DayLabel.YESTERDAY),
                        HistoryListItem.Session(previewSession(3, GameMode.CLASSIC)),
                    ),
                ),
            onStartQuiz = {},
        )
    }
}

@LumoraPreviews
@Composable
private fun HistoryEmptyPreview() {
    PreviewSurface {
        HistoryScreen(state = HistoryUiState.Empty, onStartQuiz = {})
    }
}

private fun previewSession(
    id: Long,
    mode: GameMode,
) = QuizSession(
    id = id,
    config = QuizConfig(mode, 17, "Science & Nature", Difficulty.MEDIUM, QuestionType.MULTIPLE_CHOICE),
    startedAt = Instant.parse("2026-11-26T20:00:00Z"),
    finishedAt = Instant.parse("2026-11-26T20:01:42Z"),
    localDate = LocalDate.of(2026, 11, 26),
    durationSeconds = 102,
    answerTimeMs = 40_000,
    totalQuestions = 10,
    correctCount = 8,
    wrongCount = 2,
    timeoutCount = 1,
    scorePercent = 80,
    averageAnswerMs = 4_000,
    endReason = EndReason.COMPLETED,
)
