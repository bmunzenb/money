package com.munzenberger.money.version

sealed class VersionStatus {
    data object UnsupportedVersion : VersionStatus()

    data object CurrentVersion : VersionStatus()

    abstract class RequiresUpgrade(
        val requiresInitialization: Boolean,
    ) : VersionStatus() {
        abstract fun applyUpgrade()
    }
}
