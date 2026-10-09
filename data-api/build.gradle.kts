plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutinesCore)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.serializationCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
