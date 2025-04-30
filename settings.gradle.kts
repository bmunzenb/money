pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "money"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("sql")
include("version")
include("core")
include("app-fx")
