package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.version.Version
import com.munzenberger.money.version.VersionManager

abstract class MoneyVersionManager(private val queryBuilder: VersionQueryBuilder) : VersionManager<MoneyDatabase>() {

    override fun getAppliedVersions(obj: MoneyDatabase): List<Version> {

        obj.executeUpdate(queryBuilder.create())

        return obj.getList(queryBuilder.select(), VersionResultSetMapper(queryBuilder.versionIdColumnName))
    }

    override fun onVersionApplied(obj: MoneyDatabase, version: Version) {

        obj.executeUpdate(queryBuilder.insert(version))
    }
}
