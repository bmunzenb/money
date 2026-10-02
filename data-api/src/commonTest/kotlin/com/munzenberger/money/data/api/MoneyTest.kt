package com.munzenberger.money.data.api

import java.util.Currency
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {

    private val usd = Currency.getInstance("USD")

    @Test
    fun parseConvertsToFractionalUnits() {
        assertEquals(Money(123456, usd), Money.parse("1,234.56", usd, Locale.US))
    }

    @Test
    fun parseWholeNumber() {
        assertEquals(Money(1200, usd), Money.parse("12", usd, Locale.US))
    }

    @Test
    fun parseNegative() {
        assertEquals(Money(-1234, usd), Money.parse("-12.34", usd, Locale.US))
    }

    @Test
    fun parseUsesBankersRoundingToCurrencyFractionDigits() {
        assertEquals(Money(1234, usd), Money.parse("12.345", usd, Locale.US))
        assertEquals(Money(1236, usd), Money.parse("12.355", usd, Locale.US))
        assertEquals(Money(1234, usd), Money.parse("12.344", usd, Locale.US))
        assertEquals(Money(1235, usd), Money.parse("12.3451", usd, Locale.US))
        assertEquals(Money(-1234, usd), Money.parse("-12.345", usd, Locale.US))
    }

    @Test
    fun parseUsesLocaleSeparators() {
        val eur = Currency.getInstance("EUR")
        assertEquals(Money(123456, eur), Money.parse("1.234,56", eur, Locale.GERMANY))
    }

    @Test
    fun parseCurrencyWithNoFractionDigits() {
        val jpy = Currency.getInstance("JPY")
        assertEquals(Money(1234, jpy), Money.parse("1,234.5", jpy, Locale.US))
        assertEquals(Money(1236, jpy), Money.parse("1,235.5", jpy, Locale.US))
    }

    @Test
    fun parseCurrencyWithThreeFractionDigits() {
        val bhd = Currency.getInstance("BHD")
        assertEquals(Money(1234, bhd), Money.parse("1.2345", bhd, Locale.US))
        assertEquals(Money(1236, bhd), Money.parse("1.2355", bhd, Locale.US))
    }

    @Test
    fun parseDefaultsToDefaultCurrency() {
        assertEquals(Money(1234, Money.DEFAULT_CURRENCY), Money.parse("12.34", locale = Locale.US))
    }

    @Test
    fun parseTrimsWhitespace() {
        assertEquals(Money(500, usd), Money.parse(" 5.00 ", usd, Locale.US))
    }

    @Test
    fun parseRejectsInvalidInput() {
        listOf("", "abc", "12abc", "1.2.3").forEach { input ->
            assertFailsWith<NumberFormatException>(input) { Money.parse(input, usd, Locale.US) }
        }
    }

    @Test
    fun toStringFormatsAsCurrency() {
        assertEquals("$1,234.56", Money(123456, usd).toString(Locale.US))
        assertEquals("-$12.34", Money(-1234, usd).toString(Locale.US))
        assertEquals("$0.05", Money(5, usd).toString(Locale.US))
    }

    @Test
    fun toStringFormatsAsNumber() {
        assertEquals("1,234.56", Money(123456, usd).toString(Locale.US, asCurrency = false))
        assertEquals("12.50", Money(1250, usd).toString(Locale.US, asCurrency = false))
        assertEquals("-12.34", Money(-1234, usd).toString(Locale.US, asCurrency = false))
    }

    @Test
    fun toStringUsesLocaleSeparators() {
        val eur = Currency.getInstance("EUR")
        assertEquals("1.234,56\u00A0€", Money(123456, eur).toString(Locale.GERMANY))
        assertEquals("1.234,56", Money(123456, eur).toString(Locale.GERMANY, asCurrency = false))
    }

    @Test
    fun toStringUsesMoneyCurrencyRatherThanLocaleCurrency() {
        val eur = Currency.getInstance("EUR")
        assertEquals("€1,234.56", Money(123456, eur).toString(Locale.US))
    }

    @Test
    fun toStringUsesCurrencyFractionDigits() {
        val jpy = Currency.getInstance("JPY")
        val bhd = Currency.getInstance("BHD")
        assertEquals("1,234", Money(1234, jpy).toString(Locale.US, asCurrency = false))
        assertEquals("1.234", Money(1234, bhd).toString(Locale.US, asCurrency = false))
        assertEquals("BHD1.234", Money(1234, bhd).toString(Locale.US))
    }

    @Test
    fun toStringRoundTripsThroughParse() {
        val money = Money(-123456, usd)
        assertEquals(money, Money.parse(money.toString(Locale.US, asCurrency = false), usd, Locale.US))
    }
}
