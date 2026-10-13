rootProject.name = "Money"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // The IntelliJ Platform icons that Jewel's AllIconsKeys point to aren't on Maven Central.
        maven("https://www.jetbrains.com/intellij-repository/releases") {
            mavenContent {
                includeModule("com.jetbrains.intellij.platform", "icons")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":desktop-material")
include(":desktop-jewel")
include(":presentation")
include(":data-api")
include(":data-sql")
include(":core")