plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
}

dependencies {
    api(projects.shared)
    api(projects.core)
    implementation(projects.dataSql)

    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs)
    implementation(libs.filekit.dialogs.compose)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}

detekt {
    config.setFrom(project.file("detekt.yml"))
    buildUponDefaultConfig = true
}
