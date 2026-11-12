plugins {
    alias(libs.plugins.lumen.jvm.library)
}

dependencies {
    api(projects.core.common)
    api(libs.kotlinx.coroutines.test)
}
