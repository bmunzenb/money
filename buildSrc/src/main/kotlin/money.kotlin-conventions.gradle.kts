plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

ktlint {
    // https://github.com/pinterest/ktlint/releases
    version.set("1.6.0")
}

group = "com.munzenberger"
version = "0.1-SNAPSHOT"

base {
    archivesName = "${rootProject.name}-${project.name}"
}
