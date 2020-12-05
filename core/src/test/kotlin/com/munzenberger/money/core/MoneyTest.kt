package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.ArithmeticException
import java.text.ParseException
import java.util.Currency

class MoneyTest {

    private val USD = Currency.getInstance("USD")

    @Test
    fun valueOfFraction() {

        assertEquals(250, Money.valueOf("2.50", USD).value)
        assertEquals("valueOfFraction rounds down", 250, Money.valueOf("2.501", USD).value)
        assertEquals("valueOfFraction rounds down", 250, Money.valueOf("2.509", USD).value)
    }

    @Test
    fun `valueOfFraction with invalid string`() {

        try {
            Money.valueOf("invalid", USD)
            fail("Should have thrown ParseException")
        } catch (e: ParseException) {
            // expected
        }
    }

    @Test
    fun `valueOfFraction out of bounds`() {

        try {
            Money.valueOf("1${Long.MAX_VALUE}", USD)
            fail("Should have thrown ArithmeticException")
        } catch (e: ArithmeticException) {
            // expected
        }
    }
}
