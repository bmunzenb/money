plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.detekt)
}

kotlin {
    jvm()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
