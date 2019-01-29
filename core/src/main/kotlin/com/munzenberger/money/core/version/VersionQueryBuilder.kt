package com.munzenberger.money.core.version

import com.munzenberger.money.sql.Query

class VersionQueryBuilder(val tableName: String) {

    val hashColumnName: String = "VERSION_HASH"
    val timestampColumnName: String = "VERSION_TIMESTAMP"

    fun create() = Query.createTable(tableName)
            .ifNotExists()
            .column(hashColumnName, "BIGINT NOT NULL PRIMARY KEY")
            .column(timestampColumnName, "BIGINT NOT NULL")
            .build()

    fun select() = Query.selectFrom(tableName)
            .cols(hashColumnName)
            .orderBy(timestampColumnName)
            .build()

    fun insert(hash: Long) = Query.insertInto(tableName)
            .set(hashColumnName, hash)
            .set(timestampColumnName, System.currentTimeMillis())
            .build()
}
