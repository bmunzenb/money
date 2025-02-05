package com.munzenberger.money.version

sealed class VersionStatus {
    data object UnsupportedVersion : VersionStatus()

    data object CurrentVersion : VersionStatus()

    abstract class PendingUpgrades(
        val requiresInitialization: Boolean,
    ) : VersionStatus() {
        abstract fun apply()
    }
}
