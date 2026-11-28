package sk.mpage.lumen.feature.profile.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import sk.mpage.lumen.feature.profile.R

@Composable
internal fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentName, selection = TextRange(currentName.length)))
    }
    var edited by rememberSaveable { mutableStateOf(false) }
    val error = PlayerName.validate(name.text)
    val confirm = { if (error == null) onConfirm(name.text.trim()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_rename_title)) },
        text = {
            NameField(
                value = name,
                error = error.takeIf { edited },
                onValueChange = {
                    name = it
                    edited = true
                },
                onDone = confirm,
            )
        },
        confirmButton = {
            TextButton(
                onClick = confirm,
                enabled = error == null,
            ) { Text(stringResource(R.string.profile_rename_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_rename_cancel)) }
        },
    )
}
