package sk.mpage.lumen.feature.profile.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import sk.mpage.lumen.core.designsystem.component.GlassSurface
import sk.mpage.lumen.core.designsystem.component.LocalHazeState
import sk.mpage.lumen.core.designsystem.component.LumoraDockClearance
import sk.mpage.lumen.core.designsystem.component.LumoraLargeTopBar
import sk.mpage.lumen.core.designsystem.icon.LumoraIcons
import sk.mpage.lumen.core.designsystem.layout.LumoraFlex
import sk.mpage.lumen.core.designsystem.modifier.hazeContent
import sk.mpage.lumen.core.designsystem.preview.LumoraPreviews
import sk.mpage.lumen.core.designsystem.preview.PreviewSurface
import sk.mpage.lumen.core.designsystem.theme.Lumora
import sk.mpage.lumen.core.model.Avatar
import sk.mpage.lumen.core.model.ManualCheck
import sk.mpage.lumen.core.model.ReminderStatus
import sk.mpage.lumen.core.ui.ProfileAvatar
import sk.mpage.lumen.core.ui.rememberNotificationPermissionState
import sk.mpage.lumen.feature.profile.R
import sk.mpage.lumen.feature.profile.component.AvatarPicker
import sk.mpage.lumen.feature.profile.component.RenameDialog
import sk.mpage.lumen.feature.profile.component.SettingsCard
import sk.mpage.lumen.feature.profile.component.SwitchItem
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.padding(bottom = LumoraDockClearance)) },
        topBar = { LumoraLargeTopBar(title = stringResource(R.string.profile_title), scrollBehavior = scrollBehavior) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (state) {
                ProfileUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ProfileUiState.Content -> {
                    LumoraFlex(
                        items = listOf({ IdentityCard(state, onAction) }, { NotificationsCard(state, onAction) }),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .hazeContent(LocalHazeState.current)
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    top = padding.calculateTopPadding(),
                                    start = Lumora.spacing.gutterCompact,
                                    end = Lumora.spacing.gutterCompact,
                                    bottom = padding.calculateBottomPadding() + LumoraDockClearance,
                                ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdentityCard(
    state: ProfileUiState.Content,
    onAction: (ProfileAction) -> Unit,
) {
    var renaming by rememberSaveable { mutableStateOf(false) }
    var choosingAvatar by rememberSaveable { mutableStateOf(false) }
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        material = Lumora.glass.frost,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Lumora.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Lumora.spacing.md),
        ) {
            ProfileAvatar(
                avatar = state.avatar,
                contentDescription = stringResource(R.string.profile_avatar_description, state.name),
                size = Lumora.sizes.xxxl,
            )
            Text(
                text = state.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Lumora.spacing.sm)) {
                TextButton(onClick = { choosingAvatar = true }) { Text(stringResource(R.string.profile_change_avatar)) }
                TextButton(onClick = { renaming = true }) { Text(stringResource(R.string.profile_rename)) }
            }
        }
    }
    if (renaming) {
        RenameDialog(
            currentName = state.name,
            onConfirm = {
                onAction(ProfileAction.Renamed(it))
                renaming = false
            },
            onDismiss = { renaming = false },
        )
    }
    if (choosingAvatar) {
        ModalBottomSheet(onDismissRequest = { choosingAvatar = false }) {
            AvatarPicker(
                avatar = state.avatar,
                isImportingPhoto = state.isImportingPhoto,
                onPresetSelected = { onAction(ProfileAction.PresetSelected(it)) },
                onPhotoPicked = { onAction(ProfileAction.PhotoPicked(it)) },
                modifier = Modifier.padding(Lumora.spacing.xl).safeDrawingPadding(),
            )
        }
    }
}

@Composable
private fun NotificationsCard(
    state: ProfileUiState.Content,
    onAction: (ProfileAction) -> Unit,
) {
    val permission = rememberNotificationPermissionState()
    val setReminder = { enabled: Boolean, action: (Boolean) -> ProfileAction ->
        if (enabled) {
            permission.request { granted -> if (granted) onAction(action(true)) }
        } else {
            onAction(action(false))
        }
    }
    SettingsCard(title = stringResource(R.string.profile_notifications)) {
        SwitchItem(
            title = stringResource(R.string.profile_streak_reminders),
            subtitle = stringResource(R.string.profile_streak_reminders_subtitle),
            checked = state.streakReminders && permission.isGranted,
            onCheckedChange = { setReminder(it, ProfileAction::StreakRemindersChanged) },
        )
        SwitchItem(
            title = stringResource(R.string.profile_inactivity_reminders),
            subtitle = stringResource(R.string.profile_inactivity_reminders_subtitle),
            checked = state.inactivityReminders && permission.isGranted,
            onCheckedChange = { setReminder(it, ProfileAction::InactivityRemindersChanged) },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.profile_reminder_check)) },
            supportingContent = { Text(reminderStatusText(state.reminderStatus)) },
            trailingContent = { Icon(painterResource(LumoraIcons.ArrowForward), contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onAction(ProfileAction.RunReminderCheck) },
        )
        if (permission.isBlocked) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.profile_notifications_off)) },
                supportingContent = { Text(stringResource(R.string.profile_notifications_off_subtitle)) },
                trailingContent = { Icon(painterResource(LumoraIcons.ArrowForward), contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable(onClick = permission::openSettings),
            )
        }
    }
}

@Composable
private fun reminderStatusText(status: ReminderStatus): String =
    when (status.manualCheck) {
        ManualCheck.QUEUED -> {
            stringResource(R.string.profile_reminder_queued)
        }

        ManualCheck.RUNNING -> {
            stringResource(R.string.profile_reminder_running)
        }

        else -> {
            status.nextCheckAt?.let {
                stringResource(
                    R.string.profile_reminder_next,
                    DateTimeFormatter
                        .ofLocalizedTime(FormatStyle.SHORT)
                        .format(it.atZone(ZoneId.systemDefault())),
                )
            } ?: stringResource(R.string.profile_reminder_not_scheduled)
        }
    }

@LumoraPreviews
@Composable
private fun ProfileContentPreview() {
    PreviewSurface { ProfileScreen(ProfileUiState.Content("Ada", Avatar.Preset("owl")), onAction = {}) }
}

@LumoraPreviews
@Composable
private fun ProfileLoadingPreview() {
    PreviewSurface { ProfileScreen(ProfileUiState.Loading, onAction = {}) }
}
