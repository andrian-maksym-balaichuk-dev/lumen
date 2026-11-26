package sk.mpage.lumen.feature.game.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.feature.game.R

@Composable
internal fun QuitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.game_quit_title)) },
        text = { Text(stringResource(R.string.game_quit_message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.game_quit_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.game_quit_dismiss)) } },
    )
}
