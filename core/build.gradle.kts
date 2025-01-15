plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktlint)
    id("java-test-fixtures")
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

    api(project(":sql"))
    api(project(":version"))

    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.h2)
    testFixturesImplementation(libs.sqlite.jdbc)

    testImplementation(libs.junit)
}
