plugins {
    alias(libs.plugins.lumen.jvm.library)
    alias(libs.plugins.lumen.hilt)
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
}
