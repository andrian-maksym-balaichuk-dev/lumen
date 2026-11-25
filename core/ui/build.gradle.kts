plugins {
    alias(libs.plugins.lumen.android.library.compose)
}

android {
    namespace = "sk.mpage.lumen.core.ui"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.compose)
}
