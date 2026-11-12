plugins {
    alias(libs.plugins.lumen.android.application)
    alias(libs.plugins.lumen.android.application.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "sk.mpage.lumen"

    defaultConfig {
        applicationId = "sk.mpage.lumen"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
