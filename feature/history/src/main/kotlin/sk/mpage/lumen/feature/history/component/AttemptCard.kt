package sk.mpage.lumen.feature.history.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Difficulty
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuestionType
import sk.mpage.lumen.core.ui.formatSeconds
import sk.mpage.lumen.feature.history.R
import java.time.Instant

private enum class AttemptResult { CORRECT, WRONG, TIMED_OUT }

private val QuestionAttempt.result: AttemptResult
    get() =
        when {
            selectedAnswer == null -> AttemptResult.TIMED_OUT
            isCorrect -> AttemptResult.CORRECT
            else -> AttemptResult.WRONG
        }

@Composable
internal fun AttemptCard(
    attempt: QuestionAttempt,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val seconds = formatSeconds(attempt.timeSpentMs, locale)
    val result = attempt.result
    val question = attempt.question
    val description =
        buildString {
            append(
                stringResource(
                    R.string.detail_a11y_question,
                    attempt.position,
                    question.text,
                    stringResource(result.labelRes),
                    seconds,
                ),
            )
            append(' ')
            append(
                attempt.selectedAnswer?.let { stringResource(R.string.detail_a11y_your_answer, it) }
                    ?: stringResource(R.string.detail_a11y_no_answer),
            )
            if (result != AttemptResult.CORRECT) {
                append(' ')
                append(stringResource(R.string.detail_a11y_correct_answer, question.correctAnswer))
            }
        }
    Card(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Lumora.glass.frost.tint),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Lumora.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.detail_question_number, attempt.position),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                ResultBadge(result)
                Box(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.detail_seconds, seconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(question.text, style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xs)) {
                question.options.forEach { option ->
                    OptionRow(
                        option = option,
                        isCorrect = option == question.correctAnswer,
                        isSelected = option == attempt.selectedAnswer,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultBadge(result: AttemptResult) {
    val color =
        when (result) {
            AttemptResult.CORRECT -> Lumora.colors.success
            AttemptResult.WRONG -> MaterialTheme.colorScheme.error
            AttemptResult.TIMED_OUT -> Lumora.colors.warning
        }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.12f), contentColor = color) {
        Row(
            modifier = Modifier.padding(horizontal = Lumora.spacing.sm, vertical = Lumora.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.xs),
        ) {
            Icon(painterResource(result.iconRes), contentDescription = null, modifier = Modifier.size(Lumora.sizes.xxs))
            Text(stringResource(result.labelRes), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun OptionRow(
    option: String,
    isCorrect: Boolean,
    isSelected: Boolean,
) {
    val container: Color
    val content: Color

    @DrawableRes val icon: Int?
    when {
        isCorrect -> {
            container = Lumora.colors.successContainer
            content = Lumora.colors.onSuccessContainer
            icon = LumoraIcons.CheckCircle
        }

        isSelected -> {
            container = MaterialTheme.colorScheme.errorContainer
            content = MaterialTheme.colorScheme.onErrorContainer
            icon = LumoraIcons.Cancel
        }

        else -> {
            container = Color.Transparent
            content = MaterialTheme.colorScheme.onSurfaceVariant
            icon = null
        }
    }
    val label =
        when {
            isSelected -> stringResource(R.string.detail_your_answer)
            isCorrect -> stringResource(R.string.detail_correct_answer)
            else -> null
        }
    Surface(shape = MaterialTheme.shapes.small, color = container, contentColor = content) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Lumora.spacing.md, vertical = Lumora.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            Box(Modifier.size(Lumora.sizes.xxs)) {
                if (icon != null) Icon(painterResource(icon), contentDescription = null)
            }
            Column(Modifier.weight(1f)) {
                Text(option, style = MaterialTheme.typography.bodyLarge)
                if (label != null) Text(label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private val AttemptResult.labelRes: Int
    get() =
        when (this) {
            AttemptResult.CORRECT -> R.string.detail_correct
            AttemptResult.WRONG -> R.string.detail_wrong
            AttemptResult.TIMED_OUT -> R.string.detail_timed_out
        }

private val AttemptResult.iconRes: Int
    get() =
        when (this) {
            AttemptResult.CORRECT -> LumoraIcons.CheckCircle
            AttemptResult.WRONG -> LumoraIcons.Cancel
            AttemptResult.TIMED_OUT -> LumoraIcons.TimerOff
        }

@LumoraPreviews
@Composable
private fun AttemptCardPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Lumora.spacing.gutterCompact),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            AttemptCard(previewAttempt(1, "Jupiter", 4_100))
            AttemptCard(previewAttempt(2, "Earth", 6_300))
            AttemptCard(previewAttempt(3, null, 15_000))
        }
    }
}

private fun previewAttempt(
    position: Int,
    selected: String?,
    timeMs: Long,
) = QuestionAttempt(
    position = position,
    question =
        Question(
            id = "p$position",
            text = "Which planet has the most moons?",
            options = listOf("Jupiter", "Earth", "Mars", "Venus"),
            correctAnswer = "Jupiter",
            categoryName = "Science & Nature",
            difficulty = Difficulty.MEDIUM,
            type = QuestionType.MULTIPLE_CHOICE,
        ),
    selectedAnswer = selected,
    isCorrect = selected == "Jupiter",
    timeSpentMs = timeMs,
    answeredAt = Instant.parse("2026-11-26T20:00:00Z"),
)
