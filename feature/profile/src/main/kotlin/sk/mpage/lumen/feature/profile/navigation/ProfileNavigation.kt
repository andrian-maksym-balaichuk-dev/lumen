package sk.mpage.lumen.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.feature.profile.onboarding.OnboardingRoute
import sk.mpage.lumen.feature.profile.settings.ProfileRoute

@Serializable
public data object OnboardingKey : NavKey

@Serializable
public data object ProfileKey : NavKey

public fun EntryProviderScope<NavKey>.profileEntries(onOnboardingDone: () -> Unit) {
    entry<OnboardingKey> { OnboardingRoute(onFinished = onOnboardingDone) }
    entry<ProfileKey> { ProfileRoute() }
}
