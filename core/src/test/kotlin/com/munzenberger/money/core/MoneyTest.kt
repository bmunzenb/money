package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.ArithmeticException
import java.text.ParseException
import java.util.Currency
import java.util.Locale

class MoneyTest {
    companion object {
        private val USD = Currency.getInstance("USD")
        private val LOCALE = Locale.US
    }

    @Test
    fun valueOfFraction() {
        assertEquals(250, Money.valueOf("2.50", USD, LOCALE).value)
        assertEquals("valueOfFraction rounds down", 250, Money.valueOf("2.501", USD, LOCALE).value)
        assertEquals("valueOfFraction rounds down", 250, Money.valueOf("2.509", USD, LOCALE).value)
    }

    @Test
    fun valueOfNegative() {
        assertEquals(-100, Money.valueOf("-1.00", USD, LOCALE).value)
    }

    @Test
    fun `valueOfFraction with invalid string`() {
        try {
            Money.valueOf("invalid", USD, LOCALE)
            fail("Should have thrown ParseException")
        } catch (e: ParseException) {
            // expected
        }
    }

    @Test
    fun `valueOfFraction out of bounds`() {
        try {
            Money.valueOf("1${Long.MAX_VALUE}", USD, LOCALE)
            fail("Should have thrown ArithmeticException")
        } catch (e: ArithmeticException) {
            // expected
        }
    }
}
