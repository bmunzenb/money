plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "money"

include("sql")
include("version")
include("core")
include("app-fx")
