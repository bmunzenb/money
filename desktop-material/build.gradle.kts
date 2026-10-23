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

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.material3.adaptiveNavigation3)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.components.resources)

    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.navigation3.ui)
    implementation(libs.lifecycle.viewmodelNavigation3)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs)
    implementation(libs.filekit.dialogs.compose)
}

compose.desktop {
    application {
        mainClass = "com.munzenberger.money.desktop.MainKt"

        rootProject.file("logging.local.properties").takeIf { it.exists() }?.let {
            jvmArgs("-Djava.util.logging.config.file=${it.absolutePath}")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.munzenberger.money"
            packageVersion = "1.0.0"
        }
    }
}

detekt {
    config.setFrom(project.file("detekt.yml"))
    buildUponDefaultConfig = true
}