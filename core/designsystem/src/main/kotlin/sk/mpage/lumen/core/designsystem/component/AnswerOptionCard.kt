package sk.mpage.lumen.core.designsystem.component

import androidx.annotation.DrawableRes
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
    val style = state.style()
    val scheme = MaterialTheme.colorScheme
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
                containerColor = style.container,
                contentColor = style.content,
                disabledContainerColor = style.container,
                disabledContentColor = style.content,
            ),
        border = style.border,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight).padding(Lumora.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
        ) {
            Surface(
                shape = CircleShape,
                color = if (state == AnswerState.IDLE) scheme.primaryContainer else Color.Transparent,
                contentColor = if (state == AnswerState.IDLE) scheme.onPrimaryContainer else style.content,
                border = if (state == AnswerState.IDLE) null else BorderStroke(1.dp, style.content),
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
            if (style.icon != null) {
                Icon(painterResource(style.icon), contentDescription = null, modifier = Modifier.size(Lumora.sizes.xs))
            }
        }
    }
}

private data class AnswerStyle(
    val container: Color,
    val content: Color,
    val border: BorderStroke?,
    @param:DrawableRes val icon: Int?,
)

@Composable
private fun AnswerState.style(): AnswerStyle {
    val scheme = MaterialTheme.colorScheme
    val semantic = Lumora.colors
    val frost = Lumora.glass.frost.tint
    return when (this) {
        AnswerState.IDLE -> {
            AnswerStyle(frost, scheme.onSurface, BorderStroke(1.dp, scheme.outlineVariant), null)
        }

        AnswerState.CORRECT -> {
            AnswerStyle(
                semantic.successContainer,
                semantic.onSuccessContainer,
                BorderStroke(2.dp, semantic.success),
                LumoraIcons.CheckCircle,
            )
        }

        AnswerState.WRONG -> {
            AnswerStyle(
                scheme.errorContainer,
                scheme.onErrorContainer,
                BorderStroke(2.dp, scheme.error),
                LumoraIcons.Cancel,
            )
        }

        AnswerState.REVEALED_CORRECT -> {
            AnswerStyle(
                semantic.successContainer.copy(alpha = 0.6f),
                semantic.onSuccessContainer,
                BorderStroke(1.dp, semantic.success),
                LumoraIcons.CheckCircle,
            )
        }

        AnswerState.DISABLED -> {
            AnswerStyle(frost.copy(alpha = 0.5f), scheme.onSurface.copy(alpha = 0.38f), null, null)
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
