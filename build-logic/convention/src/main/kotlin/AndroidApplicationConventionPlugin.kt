import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import sk.mpage.lumen.buildlogic.TARGET_SDK
import sk.mpage.lumen.buildlogic.configureKotlinAndroid

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = TARGET_SDK
                buildTypes {
                    debug {
                        applicationIdSuffix = ".dev"
                    }
                    release {
                        optimization {
                            enable = false
                        }
                    }
                }
                lint {
                    abortOnError = true
                    checkDependencies = true
                    error += setOf("MissingTranslation", "HardcodedText")
                }
            }
        }
    }
}
