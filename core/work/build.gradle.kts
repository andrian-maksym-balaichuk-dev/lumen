plugins {
    alias(libs.plugins.lumen.android.library)
    alias(libs.plugins.lumen.hilt)
}

android {
    namespace = "sk.mpage.lumen.core.work"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
