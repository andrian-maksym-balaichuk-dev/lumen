plugins {
    alias(libs.plugins.lumen.android.library.compose)
}

android {
    namespace = "sk.mpage.lumen.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.haze)
    implementation(libs.coil.compose)
    implementation(libs.haze.blur)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
