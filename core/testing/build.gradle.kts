plugins {
    alias(libs.plugins.lumen.jvm.library)
}

dependencies {
    api(projects.core.common)
    api(projects.core.domain)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
