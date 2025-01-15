plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
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

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
