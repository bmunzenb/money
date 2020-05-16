package com.munzenberger.money.app.property

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberStringComparatorTest {

    @Test
    fun `it sorts a list of mixed strings properly`() {

        val list: List<String?> = listOf("xyz", null, "1234", "abc123", null, "5678", "")
        val expected = listOf("1234", "5678", "", "abc123", "xyz", null, null)

        assertEquals(expected, list.sortedWith(NumberStringComparator))
    }
}
