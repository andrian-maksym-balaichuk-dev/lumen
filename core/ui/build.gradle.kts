plugins {
    alias(libs.plugins.lumen.android.library.compose)
}

android {
    namespace = "sk.mpage.lumen.core.ui"
}

dependencies {
    api(projects.core.model)

    implementation(projects.core.designsystem)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
