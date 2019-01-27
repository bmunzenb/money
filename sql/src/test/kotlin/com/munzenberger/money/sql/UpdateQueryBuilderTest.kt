package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateQueryBuilderTest {

    @Test
    fun `update values with condition`() {

        val query = Query.update("TABLE")
                .set("FOO", 1)
                .set("BAR", "FizzBuzz")
                .where("BAZ".eq(42))
                .build()

        assertEquals("UPDATE TABLE SET FOO = ?, BAR = ? WHERE BAZ = ?", query.sql)
        assertEquals(listOf(1, "FizzBuzz", 42), query.parameters)
    }
}