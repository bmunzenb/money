package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class CreateTableQueryBuilderTest {

    @Test
    fun `create table with constraints`() {

        val query = Query.createTable("FOO")
                .ifNotExists()
                .column("FIZZ", "TEXT")
                .columnWithReference("BUZZ", "INTEGER", "BAR", "BAZ")
                .constraint("C_FIZZ", "UNIQUE (FIZZ)")
                .constraint("C_BUZZ", "CHECK (BUZZ)")
                .build()

        assertEquals("CREATE TABLE IF NOT EXISTS FOO (FIZZ TEXT, BUZZ INTEGER REFERENCES BAR (BAZ), CONSTRAINT C_FIZZ UNIQUE (FIZZ), CONSTRAINT C_BUZZ CHECK (BUZZ))", query.sql)
    }
}
