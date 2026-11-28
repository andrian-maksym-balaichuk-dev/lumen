package sk.mpage.lumen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import sk.mpage.lumen.core.designsystem.theme.LumoraTheme
import sk.mpage.lumen.core.work.notification.NotificationExtras
import sk.mpage.lumen.feature.lobby.navigation.LobbyKey
import sk.mpage.lumen.feature.profile.navigation.OnboardingKey
import sk.mpage.lumen.ui.LumenRoot

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value == StartupUiState.Loading }
        enableEdgeToEdge()
        if (savedInstanceState == null) handleSessionLink(intent)
        setContent {
            val startup by viewModel.uiState.collectAsStateWithLifecycle()
            val streakAtRisk by viewModel.streakAtRisk.collectAsStateWithLifecycle()
            val sessionToOpen by viewModel.sessionToOpen.collectAsStateWithLifecycle()
            LumoraTheme {
                when (startup) {
                    StartupUiState.Loading -> {
                        Unit
                    }

                    StartupUiState.NeedsOnboarding -> {
                        LumenRoot(
                            startKey = OnboardingKey,
                            streakAtRisk = streakAtRisk,
                            sessionToOpen = null,
                            onSessionOpened = {},
                        )
                    }

                    StartupUiState.Ready -> {
                        LumenRoot(
                            startKey = LobbyKey,
                            streakAtRisk = streakAtRisk,
                            sessionToOpen = sessionToOpen,
                            onSessionOpened = viewModel::onSessionOpened,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSessionLink(intent)
    }

    private fun handleSessionLink(intent: Intent) {
        val sessionId = intent.getLongExtra(NotificationExtras.SESSION_ID, -1L)
        if (sessionId > 0) viewModel.onSessionLink(sessionId)
    }
}
