plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.javafx)
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(projects.presentation)

    implementation(libs.kotlinx.coroutinesJavafx)
    implementation(libs.ikonli.javafx)
    implementation(libs.ikonli.materialdesign2.pack)
    implementation(libs.atlantafx.base)
}

distributions.main {
    contents {
        // JetBrains' androidx artifacts on the JVM are empty redirects to Google's, whose jars have the
        // same file names, so they would otherwise be duplicate entries in lib/.
        exclude { it.file.invariantSeparatorsPath.contains("/org.jetbrains.androidx.") }
    }
}

javafx {
    version = libs.versions.javafx.get()
    modules("javafx.controls")
}

application {
    mainClass = "com.munzenberger.money.desktop.MainKt"
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics")
}
