plugins {
    alias(libs.plugins.lumen.android.feature)
}

android {
    namespace = "sk.mpage.lumen.feature.game"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
