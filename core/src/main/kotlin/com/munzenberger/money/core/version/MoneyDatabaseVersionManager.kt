package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.version.Version
import com.munzenberger.money.version.VersionManager

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
