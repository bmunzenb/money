plugins {
    `java-library`
    id("money.kotlin-conventions")
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    api(libs.kotlinx.coroutinesCore)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serializationCore)

    testImplementation(libs.kotlin.test)
}
