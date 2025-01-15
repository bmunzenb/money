plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.javafx)
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

application {
    mainClass.set("com.munzenberger.money.app.MoneyApplicationKt")
    applicationName = "Money"
}

javafx {
    version =
        libs.versions.javafx
            .asProvider()
            .get()
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {

    implementation(project(":core"))

    implementation(libs.h2)
    implementation(libs.sqlite.jdbc)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":core")))
}
