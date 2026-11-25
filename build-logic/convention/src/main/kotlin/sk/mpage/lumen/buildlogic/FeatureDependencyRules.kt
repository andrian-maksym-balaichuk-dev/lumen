package sk.mpage.lumen.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

private val forbiddenForFeatures = setOf(":core:data", ":core:database", ":core:network", ":core:datastore")

internal fun Project.enforceFeatureDependencyRules() {
    val feature = path
    configurations.configureEach {
        val configuration = name
        dependencies.configureEach {
            if (this !is ProjectDependency) return@configureEach
            val target = path
            if (target == feature) return@configureEach
            val reason =
                when {
                    target in forbiddenForFeatures -> "features reach data only through core:domain interfaces"
                    target.startsWith(":feature:") -> "features never depend on each other; wire navigation in app"
                    else -> null
                }
            if (reason != null) {
                throw GradleException("$feature must not depend on $target ($configuration): $reason (02 §2.3).")
            }
        }
    }
}
