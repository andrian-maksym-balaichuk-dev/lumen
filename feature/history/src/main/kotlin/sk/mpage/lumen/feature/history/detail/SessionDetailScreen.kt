package sk.mpage.lumen.feature.history.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import sk.mpage.lumen.core.designsystem.component.EmptyState
import sk.mpage.lumen.core.designsystem.component.LocalHazeState
import sk.mpage.lumen.core.designsystem.component.LumoraLargeTopBar
import sk.mpage.lumen.core.designsystem.component.StatTile
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.modifier.hazeContent
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.EndReason
import sk.mpage.lumen.core.model.GameMode
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.ui.formatDuration
import sk.mpage.lumen.core.ui.formatSeconds
import sk.mpage.lumen.feature.history.R
import sk.mpage.lumen.feature.history.component.AttemptCard
import sk.mpage.lumen.feature.history.component.categoryLabel
import sk.mpage.lumen.feature.history.component.difficultyLabel
import sk.mpage.lumen.feature.history.component.modeLabel
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionDetailScreen(
    state: SessionDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LumoraLargeTopBar(
                title = if (state is SessionDetailUiState.Content) categoryLabel(state.session.config) else "",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(LumoraIcons.ArrowBack), stringResource(R.string.detail_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (state) {
                SessionDetailUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                SessionDetailUiState.NotFound -> {
                    EmptyState(
                        icon = LumoraIcons.History,
                        title = stringResource(R.string.detail_not_found),
                        body = stringResource(R.string.detail_not_found_body),
                        modifier = Modifier.align(Alignment.Center).padding(top = padding.calculateTopPadding()),
                    )
                }

                is SessionDetailUiState.Content -> {
                    val itemModifier = Modifier.widthIn(max = Lumora.spacing.contentMaxWidth).fillMaxWidth()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().hazeContent(LocalHazeState.current),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding =
                            PaddingValues(
                                top = padding.calculateTopPadding(),
                                start = Lumora.spacing.gutterCompact,
                                end = Lumora.spacing.gutterCompact,
                                bottom = padding.calculateBottomPadding() + Lumora.spacing.xxl,
                            ),
                        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
                    ) {
                        item { SessionStats(state.session, itemModifier) }
                        items(state.attempts, key = { it.position }) { attempt -> AttemptCard(attempt, itemModifier) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionStats(
    session: QuizSession,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Lumora.spacing.md)) {
        Text(
            text = "${modeLabel(session.config.mode)} · ${difficultyLabel(session.config.difficulty)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
            StatTile(
                label = stringResource(R.string.detail_score),
                value =
                    if (session.config.mode == GameMode.MARATHON) {
                        session.correctCount.toString()
                    } else {
                        stringResource(R.string.history_score, session.correctCount, session.totalQuestions)
                    },
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.detail_time),
                value = formatDuration(session.durationSeconds),
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.detail_average),
                value = stringResource(R.string.detail_seconds, formatSeconds(session.averageAnswerMs, locale)),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@LumoraPreviews
@Composable
private fun SessionDetailPreview() {
    val question =
        Question(
            id = "q1",
            text = "Which planet has the most moons?",
            options = listOf("Jupiter", "Earth", "Mars", "Venus"),
            correctAnswer = "Jupiter",
            categoryName = "Science & Nature",
            difficulty = Difficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
        )
    val answeredAt = Instant.parse("2026-11-26T20:00:00Z")
    PreviewSurface {
        SessionDetailScreen(
            state =
                SessionDetailUiState.Content(
                    session =
                        QuizSession(
                            id = 1,
                            config =
                                QuizConfig(
                                    GameMode.CLASSIC,
                                    17,
                                    "Science & Nature",
                                    Difficulty.MEDIUM,
                                    QuestionType.MULTIPLE_CHOICE,
                                ),
                            startedAt = answeredAt,
                            finishedAt = answeredAt.plusSeconds(102),
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
                        ),
                    attempts =
                        persistentListOf(
                            QuestionAttempt(1, question, "Jupiter", true, 4_100, answeredAt),
                            QuestionAttempt(2, question.copy(id = "q2"), "Earth", false, 6_300, answeredAt),
                            QuestionAttempt(3, question.copy(id = "q3"), null, false, 15_000, answeredAt),
                        ),
                ),
            onBack = {},
        )
    }
}
