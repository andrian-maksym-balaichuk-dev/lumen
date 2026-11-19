import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import sk.mpage.lumen.buildlogic.configureKotlinAndroid

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                // Robolectric tests read the merged Android resources of the module.
                testOptions.unitTests.isIncludeAndroidResources = true
            }
            // Robolectric refuses SDK 35+ sandboxes on a JVM older than 21.
            val toolchains = extensions.getByType<JavaToolchainService>()
            tasks.withType<Test>().configureEach {
                javaLauncher.set(
                    toolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(ROBOLECTRIC_JVM_VERSION)) },
                )
            }
        }
    }

    private companion object {
        const val ROBOLECTRIC_JVM_VERSION = 21
    }
}
