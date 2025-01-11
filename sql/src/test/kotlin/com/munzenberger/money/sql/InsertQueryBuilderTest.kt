package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class InsertQueryBuilderTest {

    @Test
    fun `insert values`() {

        val query = insertQuery("TABLE") {
            set("FOO", 1)
            set("BAR", "FizzBuzz")
        }

        assertEquals("INSERT INTO TABLE (FOO, BAR) VALUES (?, ?)", query.sql)
        assertEquals(listOf(1, "FizzBuzz"), query.parameters)
    }
}
