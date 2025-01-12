package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class CreateTableQueryBuilderTest {
    @Test
    fun `create table with constraints`() {
        val query =
            createTableQuery("FOO") {
                ifNotExists()
                column("FIZZ", "TEXT")
                column("BUZZ", "INTEGER") {
                    references("BAR", "BAZ")
                }
                constraint("C_FIZZ", "UNIQUE (FIZZ)")
                constraint("C_BUZZ", "CHECK (BUZZ)")
            }

        @Suppress("ktlint:standard:max-line-length")
        assertEquals(
            "CREATE TABLE IF NOT EXISTS FOO (FIZZ TEXT, BUZZ INTEGER REFERENCES BAR (BAZ), CONSTRAINT C_FIZZ UNIQUE (FIZZ), CONSTRAINT C_BUZZ CHECK (BUZZ))",
            query.sql,
        )
    }
}
