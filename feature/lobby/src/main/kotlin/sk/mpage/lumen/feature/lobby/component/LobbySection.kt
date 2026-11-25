package sk.mpage.lumen.feature.lobby.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import sk.mpage.lumen.core.designsystem.theme.Lumora

@Composable
internal fun LobbySection(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        content()
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
