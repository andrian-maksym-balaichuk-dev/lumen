package sk.mpage.lumen.feature.awards.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import sk.mpage.lumen.feature.awards.AwardsRoute

@Serializable
public data object AwardsKey : NavKey

public fun EntryProviderScope<NavKey>.awardsEntries() {
    entry<AwardsKey> { AwardsRoute() }
}
