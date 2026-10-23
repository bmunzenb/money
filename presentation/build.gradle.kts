plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
}

dependencies {
    api(projects.core)

    api(libs.compose.runtime)
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(libs.compose.uiToolingPreview)
    api(libs.androidx.lifecycle.viewmodelCompose)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.navigation3.ui)
    api(libs.lifecycle.viewmodelNavigation3)
    api(libs.koin.compose)
    api(libs.koin.compose.viewmodel)
    implementation(projects.dataSql)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}

detekt {
    config.setFrom(project.file("detekt.yml"))
    buildUponDefaultConfig = true
}
