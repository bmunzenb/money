plugins {
    id("money.kotlin-conventions")
    id("java-test-fixtures")
}

dependencies {

    api(project(":sql"))
    api(project(":version"))

    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.h2)
    testFixturesImplementation(libs.sqlite.jdbc)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
