package com.munzenberger.money.data.api

import java.util.Currency
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoneyTest {

    private val usd = Currency.getInstance("USD")

    @Test
    fun `parse converts to fractional units`() {
        assertEquals(Money(123456, usd), Money.parse("1,234.56", usd, Locale.US))
    }

    @Test
    fun `parse parses a whole number`() {
        assertEquals(Money(1200, usd), Money.parse("12", usd, Locale.US))
    }

    @Test
    fun `parse parses a negative amount`() {
        assertEquals(Money(-1234, usd), Money.parse("-12.34", usd, Locale.US))
    }

    @Test
    fun `parse uses banker's rounding to currency fraction digits`() {
        assertEquals(Money(1234, usd), Money.parse("12.345", usd, Locale.US))
        assertEquals(Money(1236, usd), Money.parse("12.355", usd, Locale.US))
        assertEquals(Money(1234, usd), Money.parse("12.344", usd, Locale.US))
        assertEquals(Money(1235, usd), Money.parse("12.3451", usd, Locale.US))
        assertEquals(Money(-1234, usd), Money.parse("-12.345", usd, Locale.US))
    }

    @Test
    fun `parse uses locale separators`() {
        val eur = Currency.getInstance("EUR")
        assertEquals(Money(123456, eur), Money.parse("1.234,56", eur, Locale.GERMANY))
    }

    @Test
    fun `parse parses a currency with no fraction digits`() {
        val jpy = Currency.getInstance("JPY")
        assertEquals(Money(1234, jpy), Money.parse("1,234.5", jpy, Locale.US))
        assertEquals(Money(1236, jpy), Money.parse("1,235.5", jpy, Locale.US))
    }

    @Test
    fun `parse parses a currency with three fraction digits`() {
        val bhd = Currency.getInstance("BHD")
        assertEquals(Money(1234, bhd), Money.parse("1.2345", bhd, Locale.US))
        assertEquals(Money(1236, bhd), Money.parse("1.2355", bhd, Locale.US))
    }

    @Test
    fun `parse defaults to the default currency`() {
        assertEquals(Money(1234, Money.DEFAULT_CURRENCY), Money.parse("12.34", locale = Locale.US))
    }

    @Test
    fun `parse trims whitespace`() {
        assertEquals(Money(500, usd), Money.parse(" 5.00 ", usd, Locale.US))
    }

    @Test
    fun `parse rejects invalid input`() {
        listOf("", "abc", "12abc", "1.2.3").forEach { input ->
            assertFailsWith<NumberFormatException>(input) { Money.parse(input, usd, Locale.US) }
        }
    }

    @Test
    fun `parse throws on overflow`() {
        assertFailsWith<ArithmeticException> { Money.parse("100000000000000000000", usd, Locale.US) }
    }

    @Test
    fun `parseOrNull parses valid input`() {
        assertEquals(Money(123456, usd), Money.parseOrNull("1,234.56", usd, Locale.US))
    }

    @Test
    fun `parseOrNull returns null for invalid input`() {
        listOf("", "abc", "12abc", "1.2.3").forEach { input ->
            assertEquals(null, Money.parseOrNull(input, usd, Locale.US), input)
        }
    }

    @Test
    fun `parseOrNull returns null on overflow`() {
        assertEquals(null, Money.parseOrNull("100000000000000000000", usd, Locale.US))
    }

    @Test
    fun `toString formats as currency`() {
        assertEquals("$1,234.56", Money(123456, usd).toString(Locale.US))
        assertEquals("-$12.34", Money(-1234, usd).toString(Locale.US))
        assertEquals("$0.05", Money(5, usd).toString(Locale.US))
    }

    @Test
    fun `toString formats as account currency`() {
        assertEquals("$1,234.56", Money(123456, usd).toString(Locale.US, accountFormat = true))
        assertEquals("($12.34)", Money(-1234, usd).toString(Locale.US, accountFormat = true))
        assertEquals("$0.05", Money(5, usd).toString(Locale.US, accountFormat = true))
    }

    @Test
    fun `toString formats as account number`() {
        assertEquals("1,234.56", Money(123456, usd).toString(Locale.US, asCurrency = false, accountFormat = true))
        assertEquals("-12.34", Money(-1234, usd).toString(Locale.US, asCurrency = false, accountFormat = true))
    }

    @Test
    fun `toString formats as number`() {
        assertEquals("1,234.56", Money(123456, usd).toString(Locale.US, asCurrency = false))
        assertEquals("12.50", Money(1250, usd).toString(Locale.US, asCurrency = false))
        assertEquals("-12.34", Money(-1234, usd).toString(Locale.US, asCurrency = false))
    }

    @Test
    fun `toString uses locale separators`() {
        val eur = Currency.getInstance("EUR")
        assertEquals("1.234,56\u00A0€", Money(123456, eur).toString(Locale.GERMANY))
        assertEquals("1.234,56", Money(123456, eur).toString(Locale.GERMANY, asCurrency = false))
    }

    @Test
    fun `toString uses money currency rather than locale currency`() {
        val eur = Currency.getInstance("EUR")
        assertEquals("€1,234.56", Money(123456, eur).toString(Locale.US))
    }

    @Test
    fun `toString uses currency fraction digits`() {
        val jpy = Currency.getInstance("JPY")
        val bhd = Currency.getInstance("BHD")
        assertEquals("1,234", Money(1234, jpy).toString(Locale.US, asCurrency = false))
        assertEquals("1.234", Money(1234, bhd).toString(Locale.US, asCurrency = false))
        assertEquals("BHD1.234", Money(1234, bhd).toString(Locale.US))
    }

    @Test
    fun `toString round trips through parse`() {
        val money = Money(-123456, usd)
        assertEquals(money, Money.parse(money.toString(Locale.US, asCurrency = false), usd, Locale.US))
    }

    @Test
    fun `plus adds values`() {
        assertEquals(Money(750, usd), Money(1000, usd) + Money(-250, usd))
    }

    @Test
    fun `plus rejects different currencies`() {
        assertFailsWith<IllegalArgumentException> {
            Money(100, usd) + Money(100, Currency.getInstance("EUR"))
        }
    }

    @Test
    fun `plus throws on overflow`() {
        assertFailsWith<ArithmeticException> {
            Money(Long.MAX_VALUE, usd) + Money(1, usd)
        }
    }

    @Test
    fun `unaryMinus negates value and keeps currency`() {
        val eur = Currency.getInstance("EUR")
        assertEquals(Money(-1000, eur), -Money(1000, eur))
        assertEquals(Money(250, eur), -Money(-250, eur))
    }

    @Test
    fun `unaryMinus throws on overflow`() {
        assertFailsWith<ArithmeticException> {
            -Money(Long.MIN_VALUE, usd)
        }
    }

    @Test
    fun `compareTo orders by value within a currency`() {
        assertTrue(Money(-100, usd) < Money(50, usd))
        assertEquals(0, Money(50, usd).compareTo(Money(50, usd)))
    }

    @Test
    fun `compareTo orders by currency code before value`() {
        val eur = Currency.getInstance("EUR")
        assertTrue(Money(1000, eur) < Money(1, usd))
        assertTrue(Money(1, eur).compareTo(Money(1, usd)) != 0)
    }
}
