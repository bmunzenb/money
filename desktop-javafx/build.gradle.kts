plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.javafx)
}

javafx {
    version = libs.versions.javafx.get()
    modules("javafx.controls")
}

application {
    mainClass = "com.munzenberger.money.desktop.MainKt"
}
