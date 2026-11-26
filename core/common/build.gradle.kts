plugins {
    alias(libs.plugins.lumen.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.javax.inject)
    api(libs.kotlinx.serialization.core)
}
