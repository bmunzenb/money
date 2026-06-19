plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.repositoryApi)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutinesExtensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.sqldelight.sqliteDriver)
        }
    }
}

sqldelight {
    databases {
        create("MoneyDatabase") {
            packageName.set("com.munzenberger.money.repository.sql")
        }
    }
}
