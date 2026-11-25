import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import sk.mpage.lumen.buildlogic.enforceFeatureDependencyRules
import sk.mpage.lumen.buildlogic.libs

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("lumen.android.library.compose")
            pluginManager.apply("lumen.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            enforceFeatureDependencyRules()

            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:model"))
                "implementation"(project(":core:common"))
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
                "implementation"(libs.findLibrary("androidx-navigation3-runtime").get())
                "implementation"(libs.findLibrary("hilt-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("kotlinx-collections-immutable").get())
                "implementation"(libs.findLibrary("kotlinx-serialization-core").get())

                "testImplementation"(project(":core:testing"))
                "testImplementation"(libs.findLibrary("junit").get())
                "testImplementation"(libs.findLibrary("kotlin-test-junit").get())
                "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
                "testImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}
