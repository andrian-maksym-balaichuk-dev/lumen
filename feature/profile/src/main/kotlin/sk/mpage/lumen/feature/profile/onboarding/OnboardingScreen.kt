package sk.mpage.lumen.feature.profile.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
            val gutter = Lumora.spacing.gutterCompact
            val paneGap = Lumora.spacing.xxxl
            val sideBySide = maxWidth >= PANE_MIN_WIDTH * 2 + paneGap + gutter * 2
            val previewSize = (maxHeight * 0.2f).coerceIn(PREVIEW_MIN_SIZE, PREVIEW_MAX_SIZE)
            if (sideBySide) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = gutter),
                    horizontalArrangement = Arrangement.spacedBy(paneGap, Alignment.CenterHorizontally),
                ) {
                    Pane {
                        Welcome()
                        NameInput(state, onAction)
                        GetStartedButton(state, onAction)
                    }
                    Pane {
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            material = Lumora.glass.frost,
                            shape = MaterialTheme.shapes.extraLarge,
                        ) {
                            Avatars(state, onAction, previewSize, Modifier.padding(Lumora.spacing.xl))
                        }
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
                    ProfileCard(state, onAction, previewSize)
                    GetStartedButton(state, onAction)
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).safeDrawingPadding())
    }
}

@Composable
private fun RowScope.Pane(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .weight(1f)
                .widthIn(max = Lumora.spacing.contentMaxWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = Lumora.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Lumora.spacing.xl, Alignment.CenterVertically),
        content = content,
    )
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
    previewSize: Dp,
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
            Avatars(state, onAction, previewSize)
            NameInput(state, onAction)
        }
    }
}

@Composable
private fun Avatars(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    previewSize: Dp,
    modifier: Modifier = Modifier,
) {
    AvatarPicker(
        avatar = state.avatar,
        isImportingPhoto = state.isImportingPhoto,
        onPresetSelected = { onAction(OnboardingAction.PresetSelected(it)) },
        onPhotoPicked = { onAction(OnboardingAction.PhotoPicked(it)) },
        previewSize = previewSize,
        modifier = modifier,
    )
}

@Composable
private fun NameInput(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    NameField(
        value = state.name,
        error = state.nameError,
        onValueChange = { onAction(OnboardingAction.NameChanged(it)) },
        onDone = { onAction(OnboardingAction.Submit) },
    )
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

private val PANE_MIN_WIDTH = 280.dp
private val PREVIEW_MIN_SIZE = 72.dp
private val PREVIEW_MAX_SIZE = 112.dp

@Preview(widthDp = 914, heightDp = 411)
@Composable
private fun OnboardingLandscapePhonePreview() {
    PreviewSurface { OnboardingScreen(OnboardingUiState(name = "Ada"), onAction = {}) }
}

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
