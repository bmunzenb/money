plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.detekt)
}

dependencies {
    api(projects.dataApi)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
}
