plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
}

dependencies {
    api(libs.kotlinx.coroutinesCore)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serializationCore)

    testImplementation(libs.kotlin.test)
}
