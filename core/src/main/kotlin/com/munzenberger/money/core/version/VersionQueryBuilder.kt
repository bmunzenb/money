package com.munzenberger.money.core.version

import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.createTableQuery
import com.munzenberger.money.version.Version

class VersionQueryBuilder(val tableName: String) {

    val versionIdColumnName: String = "VERSION_ID"
    val timestampColumnName: String = "VERSION_TIMESTAMP"

    fun create() = createTableQuery(tableName) {
        ifNotExists()
        column(versionIdColumnName, "BIGINT NOT NULL PRIMARY KEY")
        column(timestampColumnName, "BIGINT NOT NULL")
    }

    fun select() = Query.selectFrom(tableName)
            .cols(versionIdColumnName)
            .orderBy(timestampColumnName)
            .build()

    fun insert(version: Version) = Query.insertInto(tableName)
            .set(versionIdColumnName, version.versionId)
            .set(timestampColumnName, System.currentTimeMillis())
            .build()
}
