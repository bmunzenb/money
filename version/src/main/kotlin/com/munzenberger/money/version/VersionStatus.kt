package com.munzenberger.money.version

sealed class VersionStatus {
    object UnsupportedVersion : VersionStatus()

    object CurrentVersion : VersionStatus()

    abstract class PendingUpgrades(
        val isFirstUse: Boolean,
    ) : VersionStatus() {
        abstract fun apply()
    }
}
