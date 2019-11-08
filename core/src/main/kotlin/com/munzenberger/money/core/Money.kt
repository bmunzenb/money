package com.munzenberger.money.core

import java.lang.UnsupportedOperationException
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class Money private constructor(private val currency: Currency, val value: Long): Comparable<Money> {

    companion object {

        val ZERO = Money.valueOf(0)

        fun valueOf(value: Long) = Money(Currency.getInstance(Locale.getDefault()), value)

        fun valueOfFraction(fraction: String): Money {

            val locale = Locale.getDefault()
            return valueOfFraction(locale, Currency.getInstance(locale), fraction)
        }

        private fun valueOfFraction(locale: Locale, currency: Currency, fraction: String): Money {

            val format = NumberFormat.getNumberInstance(locale) as DecimalFormat
            format.maximumFractionDigits = currency.defaultFractionDigits
            format.isParseBigDecimal = true

            val f = format.parse(fraction) as BigDecimal

            val v = fractionToValue(currency, f)

            return Money(currency, v)
        }

        private fun fractionToValue(currency: Currency, fraction: BigDecimal): Long {

            val multiplier = getMultiplier(currency.defaultFractionDigits)

            val m = BigDecimal.valueOf(multiplier.toLong())

            return fraction.multiply(m).longValueExact()
        }

        private fun valueToFraction(currency: Currency, value: Long): BigDecimal {

            val divisor = getMultiplier(currency.defaultFractionDigits)

            val d = BigDecimal.valueOf(divisor.toLong())
            val b = BigDecimal.valueOf(value)

            return b.divide(d)
        }

        private fun getMultiplier(fractionDigits: Int): Int {

            var multiplier = 1

            repeat(fractionDigits) { multiplier *= 10 }

            return multiplier
        }
    }

    override fun compareTo(other: Money) = when {
        other.value > this.value -> -1
        other.value < this.value -> 1
        else -> 0
    }

    fun add(money: Money): Money {

        if (currency != money.currency) {
            throw UnsupportedOperationException("Can't add money values of different currencies: $currency != ${money.currency}")
        }

        val sum = value + money.value
        return Money(currency, sum)
    }

    override fun toString() = toString(Locale.getDefault())

    fun toString(locale: Locale): String {

        val fraction = valueToFraction(currency, value)

        val format = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
        }

        return format.format(fraction)
    }

    fun toStringWithoutCurrency() = toStringWithoutCurrency(Locale.getDefault())

    fun toStringWithoutCurrency(locale: Locale): String {

        val fraction = valueToFraction(currency, value)

        val format = NumberFormat.getInstance(locale).apply {
            maximumFractionDigits = currency.defaultFractionDigits
            minimumFractionDigits = currency.defaultFractionDigits
        }

        return format.format(fraction)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Money

        if (currency != other.currency) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
