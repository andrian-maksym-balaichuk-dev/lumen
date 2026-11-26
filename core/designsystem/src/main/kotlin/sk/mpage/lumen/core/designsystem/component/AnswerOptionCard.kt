package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

public enum class AnswerState {
    IDLE,
    CORRECT,
    WRONG,
    REVEALED_CORRECT,
    DISABLED,
}

@Composable
public fun AnswerOptionCard(
    letter: String,
    text: String,
    state: AnswerState,
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    stateDescription: String? = null,
    minHeight: Dp = Lumora.sizes.xl,
) {
    val scheme = MaterialTheme.colorScheme
    val semantic = Lumora.colors
    val container =
        when (state) {
            AnswerState.IDLE -> {
                Lumora.glass.frost.tint
            }

            AnswerState.CORRECT -> {
                semantic.successContainer
            }

            AnswerState.WRONG -> {
                scheme.errorContainer
            }

            AnswerState.REVEALED_CORRECT -> {
                semantic.successContainer.copy(alpha = 0.6f)
            }

            AnswerState.DISABLED -> {
                Lumora.glass.frost.tint
                    .copy(alpha = 0.5f)
            }
        }
    val content =
        when (state) {
            AnswerState.IDLE -> scheme.onSurface
            AnswerState.CORRECT, AnswerState.REVEALED_CORRECT -> semantic.onSuccessContainer
            AnswerState.WRONG -> scheme.onErrorContainer
            AnswerState.DISABLED -> scheme.onSurface.copy(alpha = 0.38f)
        }
    val border =
        when (state) {
            AnswerState.IDLE -> BorderStroke(1.dp, scheme.outlineVariant)
            AnswerState.CORRECT -> BorderStroke(2.dp, semantic.success)
            AnswerState.WRONG -> BorderStroke(2.dp, scheme.error)
            AnswerState.REVEALED_CORRECT -> BorderStroke(1.dp, semantic.success)
            AnswerState.DISABLED -> null
        }
    val icon =
        when (state) {
            AnswerState.CORRECT, AnswerState.REVEALED_CORRECT -> LumoraIcons.CheckCircle
            AnswerState.WRONG -> LumoraIcons.Cancel
            AnswerState.IDLE, AnswerState.DISABLED -> null
        }
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = description
                    stateDescription?.let { this.stateDescription = it }
                },
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = container,
                contentColor = content,
                disabledContainerColor = container,
                disabledContentColor = content,
            ),
        border = border,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight).padding(Lumora.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
        ) {
            Surface(
                shape = CircleShape,
                color = if (state == AnswerState.IDLE) scheme.secondaryContainer else Color.Transparent,
                contentColor = if (state == AnswerState.IDLE) scheme.onSecondaryContainer else content,
                border = if (state == AnswerState.IDLE) null else BorderStroke(1.dp, content),
                modifier = Modifier.size(Lumora.sizes.sm),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(letter, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            if (icon != null) {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(Lumora.sizes.xs))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AnswerOptionCardPreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Lumora.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
        ) {
            AnswerState.entries.forEachIndexed { index, state ->
                AnswerOptionCard(
                    letter = ('A' + index).toString(),
                    text = state.name.lowercase().replace('_', ' '),
                    state = state,
                    onClick = {},
                    description = state.name,
                )
            }
        }
    }
}
