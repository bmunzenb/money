package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class ConditionTest {

    @Test
    fun `in group with varargs`() {

        val result = "COLUMN".inGroup(1, 2, 3)

        assertEquals("COLUMN IN (?, ?, ?)", result.clause)
        assertEquals(listOf(1, 2, 3), result.parameters)
    }

    @Test
    fun `not in group with varargs`() {

        val result = "COLUMN".notInGroup(1, 2, 3)

        assertEquals("COLUMN NOT IN (?, ?, ?)", result.clause)
        assertEquals(listOf(1, 2, 3), result.parameters)
    }
}
