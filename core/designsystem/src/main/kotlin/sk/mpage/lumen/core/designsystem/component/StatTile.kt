package sk.mpage.lumen.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
public fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    highlighted: Boolean = false,
) {
    val colors =
        if (highlighted) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        } else {
            CardDefaults.cardColors(
                containerColor = Lumora.glass.frost.tint,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    Card(
        modifier = modifier.semantics(mergeDescendants = true) {},
        colors = colors,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Lumora.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xs),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
            if (supporting != null) Text(supporting, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@PreviewLightDark
@Composable
private fun StatTilePreview() {
    PreviewSurface {
        Row(
            modifier = Modifier.padding(Lumora.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm),
        ) {
            StatTile("Correct", "8", Modifier.weight(1f))
            StatTile("Wrong", "2", Modifier.weight(1f), supporting = "1 timed out")
            StatTile("Accuracy", "80 %", Modifier.weight(1f), highlighted = true)
        }
    }
}
