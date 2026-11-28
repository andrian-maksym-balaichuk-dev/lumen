package sk.mpage.lumen.feature.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import sk.mpage.lumen.core.designsystem.component.GlassSurface
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.feature.profile.R

@Composable
internal fun ReminderExplainer(
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        material = Lumora.glass.frost,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(Lumora.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.lg),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(Lumora.sizes.xxl),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(LumoraIcons.Flame),
                        contentDescription = null,
                        tint = Lumora.colors.streakFlame,
                        modifier = Modifier.size(Lumora.sizes.sm),
                    )
                }
            }
            Text(
                text = stringResource(R.string.reminders_explainer_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.reminders_explainer_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onAllow,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(Lumora.sizes.lg),
            ) {
                Text(stringResource(R.string.reminders_allow), style = MaterialTheme.typography.titleMedium)
            }
            TextButton(onClick = onNotNow) { Text(stringResource(R.string.reminders_not_now)) }
        }
    }
}

@LumoraPreviews
@Composable
private fun ReminderExplainerPreview() {
    PreviewSurface { ReminderExplainer(onAllow = {}, onNotNow = {}, modifier = Modifier.padding(Lumora.spacing.lg)) }
}
