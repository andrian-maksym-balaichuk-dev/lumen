package sk.mpage.lumen.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun ErrorState(
    title: String,
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = LumoraIcons.Error,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        colors =
            CardDefaults.cardColors(
                containerColor = colors.errorContainer,
                contentColor = colors.onErrorContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(Lumora.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
            ) {
                Icon(painterResource(icon), contentDescription = null)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
            ) {
                if (secondaryLabel != null) {
                    TextButton(
                        onClick = onSecondary,
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.onErrorContainer),
                    ) {
                        Text(secondaryLabel)
                    }
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                ) {
                    Text(retryLabel)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    PreviewSurface {
        Column(
            modifier = Modifier.padding(Lumora.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.lg),
        ) {
            ErrorState(
                title = "Couldn't load categories",
                message = "You're offline. Check your connection and try again.",
                retryLabel = "Retry",
                onRetry = {},
                icon = LumoraIcons.CloudOff,
            )
            ErrorState(
                title = "Couldn't load categories",
                message = "The quiz server took too long to answer.",
                retryLabel = "Retry",
                onRetry = {},
            )
        }
    }
}
