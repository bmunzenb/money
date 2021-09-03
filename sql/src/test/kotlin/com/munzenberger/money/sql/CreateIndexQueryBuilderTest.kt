package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class CreateIndexQueryBuilderTest {

    @Test
    fun `create index`() {

        val query = CreateIndexQueryBuilder("indexName", "tableName")
                .column("columnA")
                .column("columnB")
                .build()

        assertEquals("CREATE INDEX indexName ON tableName (columnA, columnB)", query.sql)
    }

    @Test
    fun `create unique index`() {

        val query = CreateIndexQueryBuilder("indexName", "tableName")
                .unique()
                .column("columnA")
                .column("columnB")
                .build()

        assertEquals("CREATE UNIQUE INDEX indexName ON tableName (columnA, columnB)", query.sql)
    }
}
