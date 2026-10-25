plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.javafx)
}

dependencies {
    implementation(projects.presentation)

    implementation(libs.kotlinx.coroutinesJavafx)
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
