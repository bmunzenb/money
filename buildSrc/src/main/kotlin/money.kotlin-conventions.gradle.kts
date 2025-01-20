plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

ktlint {
    version = "1.5.0"
}

group = "com.munzenberger"
version = "0.1-SNAPSHOT"

base {
    archivesName = "${rootProject.name}-${project.name}"
}

dependencyLocking {
    lockAllConfigurations()
}
