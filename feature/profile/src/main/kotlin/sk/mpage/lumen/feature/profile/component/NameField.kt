package sk.mpage.lumen.feature.profile.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import sk.mpage.lumen.feature.profile.R

internal enum class NameError {
    BLANK,
    TOO_LONG,
}

internal object PlayerName {
    const val MAX_LENGTH = 24

    fun validate(raw: String): NameError? {
        val name = raw.trim()
        return when {
            name.isEmpty() -> NameError.BLANK
            name.codePointCount(0, name.length) > MAX_LENGTH -> NameError.TOO_LONG
            else -> null
        }
    }

    fun length(raw: String): Int = raw.trim().let { it.codePointCount(0, it.length) }
}

@Composable
internal fun NameField(
    value: String,
    error: NameError?,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.profile_name_label)) },
        singleLine = true,
        isError = error != null,
        supportingText = {
            Text(
                when (error) {
                    NameError.BLANK -> {
                        stringResource(R.string.profile_name_error_blank)
                    }

                    NameError.TOO_LONG -> {
                        pluralStringResource(
                            R.plurals.profile_name_error_too_long,
                            PlayerName.MAX_LENGTH,
                            PlayerName.MAX_LENGTH,
                        )
                    }

                    null -> {
                        stringResource(
                            R.string.profile_name_counter,
                            PlayerName.length(value),
                            PlayerName.MAX_LENGTH,
                        )
                    }
                },
            )
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}
