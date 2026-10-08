package com.munzenberger.money.desktop.accounts

import kotlin.test.Test
import kotlin.test.assertEquals

class AccountLabelsTest {

    @Test
    fun `masks all but the last four characters`() {
        assertEquals("••••6789", maskAccountNumber("123456789"))
    }

    @Test
    fun `masks a number of four or fewer characters`() {
        assertEquals("••••12", maskAccountNumber("12"))
    }

    @Test
    fun `ignores surrounding whitespace`() {
        assertEquals("••••6789", maskAccountNumber(" 123456789 "))
    }

    @Test
    fun `is empty for a missing or blank number`() {
        assertEquals("", maskAccountNumber(null))
        assertEquals("", maskAccountNumber("  "))
    }
}
