import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
}

dependencies {
    implementation(projects.presentation)

    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.jewel.intUiStandalone)
    implementation(libs.intellijPlatform.icons)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.munzenberger.money.jewel.MainKt"

        rootProject.file("logging.local.properties").takeIf { it.exists() }?.let {
            jvmArgs("-Djava.util.logging.config.file=${it.absolutePath}")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.munzenberger.money.jewel"
            packageVersion = "1.0.0"
        }
    }
}

// Jewel only supports the JetBrains Runtime (JBR). Compilation works with any JDK, so only the run tasks
// need the JBR. The Compose plugin sets the run tasks' executable, which has to match the launcher, so it's
// replaced when the task runs; that way a build that doesn't run the app needs no JBR.
tasks.withType<JavaExec>().configureEach {
    val jbrLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.JETBRAINS
    }
    javaLauncher = jbrLauncher
    doFirst {
        (this as JavaExec).setExecutable(jbrLauncher.get().executablePath.asFile.absolutePath)
    }
}

detekt {
    config.setFrom(project.file("detekt.yml"))
    buildUponDefaultConfig = true
}
