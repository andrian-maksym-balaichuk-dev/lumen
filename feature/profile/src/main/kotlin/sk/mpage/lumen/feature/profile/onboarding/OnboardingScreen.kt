package sk.mpage.lumen.feature.profile.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import sk.mpage.lumen.core.designsystem.component.GlassSurface
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.AvatarPicker
import sk.mpage.lumen.feature.profile.component.NameError
import sk.mpage.lumen.feature.profile.component.NameField

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            val wide = maxWidth >= WIDE_LAYOUT_MIN_WIDTH
            val gutter = if (wide) Lumora.spacing.gutterMedium else Lumora.spacing.gutterCompact
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = gutter),
                    horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.xxxl),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Welcome(Modifier.weight(1f))
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = gutter),
                        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xxl),
                    ) {
                        ProfileCard(state, onAction)
                        GetStartedButton(state, onAction)
                    }
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(gutter)
                            .widthIn(max = Lumora.spacing.contentMaxWidth)
                            .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xxl),
                ) {
                    Welcome(Modifier.padding(top = Lumora.spacing.xxl))
                    ProfileCard(state, onAction)
                    GetStartedButton(state, onAction)
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).safeDrawingPadding())
    }
}

@Composable
private fun Welcome(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xs)) {
        Text(
            text = stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.onboarding_app_name),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileCard(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        material = Lumora.glass.frost,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(Lumora.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xl),
        ) {
            AvatarPicker(
                avatar = state.avatar,
                isImportingPhoto = state.isImportingPhoto,
                onPresetSelected = { onAction(OnboardingAction.PresetSelected(it)) },
                onPhotoPicked = { onAction(OnboardingAction.PhotoPicked(it)) },
            )
            NameField(
                value = state.name,
                error = state.nameError,
                onValueChange = { onAction(OnboardingAction.NameChanged(it)) },
                onDone = { onAction(OnboardingAction.Submit) },
            )
        }
    }
}

@Composable
private fun GetStartedButton(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    Button(
        onClick = { onAction(OnboardingAction.Submit) },
        enabled = state.canSubmit,
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(stringResource(R.string.onboarding_get_started), style = MaterialTheme.typography.titleMedium)
    }
}

private val WIDE_LAYOUT_MIN_WIDTH = 600.dp

@LumoraPreviews
@Composable
private fun OnboardingEmptyPreview() {
    PreviewSurface { OnboardingScreen(OnboardingUiState(), onAction = {}) }
}

@LumoraPreviews
@Composable
private fun OnboardingValidPreview() {
    PreviewSurface { OnboardingScreen(OnboardingUiState(name = "Ada", avatar = Avatar.Preset("owl")), onAction = {}) }
}

@LumoraPreviews
@Composable
private fun OnboardingErrorPreview() {
    PreviewSurface {
        OnboardingScreen(OnboardingUiState(name = "A".repeat(25), nameError = NameError.TOO_LONG), onAction = {})
    }
}

@LumoraPreviews
@Composable
private fun OnboardingImportingPreview() {
    PreviewSurface { OnboardingScreen(OnboardingUiState(name = "Ada", isImportingPhoto = true), onAction = {}) }
}

@LumoraPreviews
@Composable
private fun OnboardingSavingPreview() {
    PreviewSurface { OnboardingScreen(OnboardingUiState(name = "Ada", isSaving = true), onAction = {}) }
}
