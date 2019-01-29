package com.munzenberger.money.core.version

import com.munzenberger.money.sql.Query

abstract class VersionQueryBuilder {

    abstract val tableName: String

    open val hashColumnName: String = "VERSION_HASH"
    open val timestampColumnName: String = "VERSION_TIMESTAMP"

    fun create() = Query.createTable(tableName)
            .ifNotExists()
            .column(hashColumnName, "BIGINT NOT NULL")
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
