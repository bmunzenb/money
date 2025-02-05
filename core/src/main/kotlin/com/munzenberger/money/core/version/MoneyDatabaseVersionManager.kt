package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.version.Version
import com.munzenberger.money.version.VersionManager
import com.munzenberger.money.version.VersionStatus

/**
 * Used to verify the schema for a Money database is compatible with the version of the core module.
 * This must be done immediately after connecting to the Money database. Call [getVersionStatus].
 */
class MoneyDatabaseVersionManager : VersionManager<MoneyDatabase>() {
    private val queryBuilder: VersionQueryBuilder = VersionQueryBuilder("CORE_VERSIONS")

    override fun getApplicableVersions() =
        listOf(
            MoneyDatabaseVersion_1(),
        )

    override fun getAppliedVersions(obj: MoneyDatabase): List<Version> {
        obj.execute(queryBuilder.create())

        return obj.getList(queryBuilder.select(), VersionResultSetMapper(queryBuilder.versionIdColumnName))
    }

    override fun onVersionApplied(
        obj: MoneyDatabase,
        version: Version,
    ) {
        obj.executeUpdate(queryBuilder.insert(version))
    }
}

/**
 * Convenience extension function to retrieve the database version status and optionally auto initialize a new database.
 * See [MoneyDatabaseVersionManager].
 *
 * @param autoInitialize If true, will initialize the database
 * @return the [VersionStatus] of the database
 */
fun MoneyDatabase.getVersionStatus(autoInitialize: Boolean = false): VersionStatus {
    val status = MoneyDatabaseVersionManager().getVersionStatus(this)
    if (status is VersionStatus.PendingUpgrades && status.requiresInitialization && autoInitialize) {
        status.apply()
        return VersionStatus.CurrentVersion
    } else {
        return status
    }
}
