package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class InsertQueryBuilderTest {

    @Test
    fun `insert values`() {

        val query = Query.insertInto("TABLE")
                .set("FOO", 1)
                .set("BAR", "FizzBuzz")
                .build()

        assertEquals("INSERT INTO TABLE (FOO, BAR) VALUES (?, ?)", query.sql)
        assertEquals(listOf(1, "FizzBuzz"), query.parameters)
    }
}
