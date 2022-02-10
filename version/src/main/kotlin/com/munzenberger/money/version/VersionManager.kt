package com.munzenberger.money.version

abstract class VersionManager<T> {

    protected abstract fun getApplicableVersions(): List<ApplicableVersion<T>>

    protected abstract fun getAppliedVersions(obj: T): List<Version>

    protected abstract fun onVersionApplied(obj: T, version: Version)

    fun getVersionStatus(obj: T): VersionStatus {

        val applicable = getApplicableVersions()
        val applied = getAppliedVersions(obj)

        val iter1 = applied.iterator()
        val iter2 = applicable.iterator()

        // the versions must have been applied in the correct sequence
        while (iter1.hasNext() && iter2.hasNext()) {

            // their IDs must match exactly
            if (iter1.next().versionId != iter2.next().versionId) {
                return VersionStatus.UnsupportedVersion
            }
        }

        // and there can't be any additional versions not in the master list
        if (iter1.hasNext()) {
            return VersionStatus.UnsupportedVersion
        }

        // check for pending upgrades
        if (iter2.hasNext()) {
            return object : VersionStatus.PendingUpgrades(applied.isEmpty()) {
                override fun apply() = iter2.forEachRemaining {
                    it.apply(obj)
                    onVersionApplied(obj, it)
                }
            }
        }

        return VersionStatus.CurrentVersion
    }
}
