plugins {
    `java-library`
    id("money.kotlin-conventions")
}

dependencies {
    api(projects.dataApi)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.turbine)
}
