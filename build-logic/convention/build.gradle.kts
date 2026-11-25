plugins {
    `kotlin-dsl`
}

group = "sk.mpage.lumen.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id =
                libs.plugins.lumen.android.application
                    .asProvider()
                    .get()
                    .pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id =
                libs.plugins.lumen.android.application.compose
                    .get()
                    .pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id =
                libs.plugins.lumen.android.library
                    .asProvider()
                    .get()
                    .pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id =
                libs.plugins.lumen.android.library.compose
                    .get()
                    .pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id =
                libs.plugins.lumen.jvm.library
                    .get()
                    .pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("androidFeature") {
            id =
                libs.plugins.lumen.android.feature
                    .get()
                    .pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidRoom") {
            id =
                libs.plugins.lumen.android.room
                    .get()
                    .pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("hilt") {
            id =
                libs.plugins.lumen.hilt
                    .get()
                    .pluginId
            implementationClass = "HiltConventionPlugin"
        }
    }
}
