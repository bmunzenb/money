plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.detekt)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.dataApi)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.mockk)
        }
    }
}
