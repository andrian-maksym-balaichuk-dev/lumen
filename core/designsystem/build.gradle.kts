plugins {
    alias(libs.plugins.lumen.android.library.compose)
}

android {
    namespace = "sk.mpage.lumen.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
}
