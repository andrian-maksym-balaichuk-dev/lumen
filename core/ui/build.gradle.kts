plugins {
    alias(libs.plugins.lumen.android.library.compose)
}

android {
    namespace = "sk.mpage.lumen.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)

    implementation(projects.core.designsystem)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
