plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.repositoryApi)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
