package com.munzenberger.money.core.version

import com.munzenberger.money.sql.createTableQuery
import com.munzenberger.money.sql.insertQuery
import com.munzenberger.money.sql.selectQuery
import com.munzenberger.money.version.Version

class VersionQueryBuilder(val tableName: String) {
    val versionIdColumnName: String = "VERSION_ID"
    val timestampColumnName: String = "VERSION_TIMESTAMP"

    fun create() =
        createTableQuery(tableName) {
            ifNotExists()
            column(versionIdColumnName, "BIGINT NOT NULL PRIMARY KEY")
            column(timestampColumnName, "BIGINT NOT NULL")
        }

    fun select() =
        selectQuery(tableName) {
            cols(versionIdColumnName)
            orderBy(timestampColumnName)
        }

    fun insert(version: Version) =
        insertQuery(tableName) {
            set(versionIdColumnName, version.versionId)
            set(timestampColumnName, System.currentTimeMillis())
        }
}
