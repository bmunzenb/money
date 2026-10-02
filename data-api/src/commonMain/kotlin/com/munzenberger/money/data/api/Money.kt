package com.munzenberger.money.data.api

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Currency
import java.util.Locale

data class Money(
    val value: Long,
    val currency: Currency = DEFAULT_CURRENCY,
) {

    /**
     * Formats this amount for [locale], converting [value] from whole fractional units (e.g. cents
     * for USD) to the currency's major unit (e.g. dollars). When [asCurrency] is true, the result
     * is formatted as [currency] (e.g. "$1,234.56"); otherwise as a plain number (e.g. "1,234.56").
     */
    fun toString(locale: Locale = Locale.getDefault(), asCurrency: Boolean = true): String {
        val fractionDigits = currency.fractionDigits
        val format = if (asCurrency) {
            NumberFormat.getCurrencyInstance(locale).also { it.currency = currency }
        } else {
            NumberFormat.getNumberInstance(locale)
        }.apply {
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }
        return format.format(BigDecimal.valueOf(value, fractionDigits))
    }

    override fun toString(): String = toString(Locale.getDefault())

    companion object {
        val DEFAULT_CURRENCY: Currency = Currency.getInstance("USD")

        /**
         * Parses [value] as a number formatted for [locale], rounds it to the number of
         * fractional digits [currency] allows using banker's rounding (half-even), and returns
         * it in whole fractional units (e.g. cents for USD).
         *
         * @throws NumberFormatException if [value] isn't entirely a number in [locale]'s format.
         */
        fun parse(
            value: String,
            currency: Currency = DEFAULT_CURRENCY,
            locale: Locale = Locale.getDefault(),
        ): Money {
            val format = (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
                isParseBigDecimal = true
            }
            val text = value.trim()
            val position = ParsePosition(0)
            val number = format.parse(text, position) as BigDecimal?
            if (number == null || position.index != text.length) {
                throw NumberFormatException("Invalid amount \"$value\" for locale $locale")
            }

            val fractionDigits = currency.fractionDigits
            val units = number
                .setScale(fractionDigits, RoundingMode.HALF_EVEN)
                .movePointRight(fractionDigits)
                .longValueExact()

            return Money(units, currency)
        }
    }
}

/** The number of fractional digits [this] currency uses, or 0 for pseudo-currencies that have none. */
private val Currency.fractionDigits: Int
    get() = defaultFractionDigits.coerceAtLeast(0)
