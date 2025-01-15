plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.versions)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

ktlint {
    version =
        libs.versions.ktlint
            .asProvider()
            .get()
}
