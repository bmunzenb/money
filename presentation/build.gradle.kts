plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
}

dependencies {
    api(projects.core)

    api(libs.androidx.lifecycle.viewmodel)
    api(libs.koin.core.viewmodel)
    implementation(projects.dataSql)
    implementation(libs.kotlinx.serializationJson)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
