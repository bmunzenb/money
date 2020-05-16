package com.munzenberger.money.app.property

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class AsyncObjectComparatorTest {

    @Test
    fun `it sorts a list of mixed async strings properly`() {

        val a1 = AsyncObject.Complete("xyz")
        val a2 = AsyncObject.Error<String>(mockk())
        val a3 = AsyncObject.Pending<String>()
        val a4 = AsyncObject.Complete("abc")
        val a5 = AsyncObject.Executing<String>()

        val list = listOf(a1, a2, a3, a4, a5)

        val expected = listOf(a3, a5, a4, a1, a2)

        assertEquals(expected, list.sortedWith(AsyncObjectComparator()))
    }
}
