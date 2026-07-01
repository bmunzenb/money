plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.detekt)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.dataApi)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutinesExtensions)
            implementation(libs.sqldelight.sqliteDriver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}

sqldelight {
    databases {
        create("MoneyDatabase") {
            packageName.set("com.munzenberger.money.data.sql")
        }
    }
}
