plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.detekt)
}

dependencies {
    api(projects.dataApi)
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.coroutinesExtensions)
    implementation(libs.sqldelight.sqliteDriver)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutinesTest)
}

sqldelight {
    databases {
        create("MoneyDatabase") {
            packageName.set("com.munzenberger.money.data.sql")
        }
    }
}
