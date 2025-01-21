plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "com.munzenberger"
version = "0.1-SNAPSHOT"

base {
    archivesName = "${rootProject.name}-${project.name}"
}
