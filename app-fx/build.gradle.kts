plugins {
    application
    id("money.kotlin-conventions")
    alias(libs.plugins.javafx)
}

repositories {
    mavenCentral()
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
