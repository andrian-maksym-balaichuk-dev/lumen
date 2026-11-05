import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import sk.mpage.lumen.buildlogic.configureAndroidCompose

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("lumen.android.library")
            configureAndroidCompose(extensions.getByType<LibraryExtension>())
        }
    }
}
