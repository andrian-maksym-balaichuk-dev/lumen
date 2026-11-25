package sk.mpage.lumen

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
import sk.mpage.lumen.feature.profile.navigation.OnboardingKey
import sk.mpage.lumen.ui.LumenRoot
import sk.mpage.lumen.ui.navigation.LobbyKey

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value == StartupUiState.Loading }
        enableEdgeToEdge()
        setContent {
            val startup by viewModel.uiState.collectAsStateWithLifecycle()
            LumoraTheme {
                when (startup) {
                    StartupUiState.Loading -> Unit
                    StartupUiState.NeedsOnboarding -> LumenRoot(startKey = OnboardingKey)
                    StartupUiState.Ready -> LumenRoot(startKey = LobbyKey)
                }
            }
        }
    }
}
