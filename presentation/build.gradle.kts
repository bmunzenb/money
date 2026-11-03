plugins {
    `java-library`
    id("money.kotlin-conventions")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    api(projects.core)

    api(libs.androidx.lifecycle.viewmodel)
    api(libs.koin.core.viewmodel)
    implementation(libs.kotlinx.serializationJson)

    testImplementation(projects.dataSql)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
