package com.munzenberger.money.version

sealed class VersionStatus

object UnsupportedVersion : VersionStatus()

object CurrentVersion : VersionStatus()

abstract class PendingUpgrades<T> : VersionStatus() {
    abstract fun upgrade(obj: T)
}

abstract class VersionManager<T> {

    abstract fun getApplicableVersions(): List<ApplicableVersion<T>>

    abstract fun getAppliedVersions(obj: T): List<Version>

    abstract fun onVersionApplied(obj: T, version: Version)

    fun getVersionStatus(obj: T): VersionStatus {

        val applicable = getApplicableVersions()
        val applied = getAppliedVersions(obj)

        val iter1 = applied.iterator()
        val iter2 = applicable.iterator()

        // the versions must have been applied in the correct sequence
        while (iter1.hasNext() && iter2.hasNext()) {

            // their hashes must match exactly
            if (iter1.next().hash != iter2.next().hash) {
                return UnsupportedVersion
            }
        }

        // and there can't be any additional versions not in the master list
        if (!iter1.hasNext()) {
            return UnsupportedVersion
        }

        // check for pending upgrades
        if (iter2.hasNext()) {
            return object : PendingUpgrades<T>() {
                override fun upgrade(obj: T) = iter2.forEachRemaining {
                    it.apply(obj)
                    onVersionApplied(obj, it)
                }
            }
        }

        return CurrentVersion
    }
}
