package sk.mpage.lumen.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.feature.profile.onboarding.OnboardingRoute

@Serializable
public data object OnboardingKey : NavKey

public fun EntryProviderScope<NavKey>.profileEntries(onOnboardingDone: () -> Unit) {
    entry<OnboardingKey> { OnboardingRoute(onFinished = onOnboardingDone) }
}
