package com.munzenberger.money.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.Currency
import java.util.Locale

class Money private constructor(val currency: Currency, val value: Long) : Comparable<Money> {
    companion object {
        val ZERO: Money by lazy { zero() }

        private val defaultCurrency = Currency.getInstance("USD")

        private val defaultLocale = Locale.getDefault()

        fun zero(currency: Currency = defaultCurrency) = valueOf(0, currency)

        fun valueOf(
            value: Long,
            currency: Currency = defaultCurrency,
        ) = Money(currency, value)

        @Throws(ParseException::class)
        fun valueOf(
            fraction: String,
            currency: Currency = defaultCurrency,
            locale: Locale = defaultLocale,
        ): Money {
            val format =
                (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
                    isParseBigDecimal = true
                }

            val f =
                (format.parse(fraction) as BigDecimal)
                    // round down to the nearest fractional digit
                    .setScale(currency.defaultFractionDigits, RoundingMode.FLOOR)

            val v = fractionToValue(currency, f)

            return Money(currency, v)
        }

        private fun fractionToValue(
            currency: Currency,
            fraction: BigDecimal,
        ): Long {
            val multiplier = getMultiplier(currency.defaultFractionDigits)

            return fraction.multiply(multiplier).longValueExact()
        }

        private fun valueToFraction(
            currency: Currency,
            value: Long,
        ): BigDecimal {
            val divisor = getMultiplier(currency.defaultFractionDigits)

            val b = BigDecimal.valueOf(value)

            return b.divide(divisor)
        }

        private fun getMultiplier(fractionDigits: Int): BigDecimal {
            var multiplier = BigDecimal.ONE

            repeat(fractionDigits) { multiplier *= BigDecimal.TEN }

            return multiplier
        }
    }

    override fun compareTo(other: Money) =
        when {
            other.value > this.value -> -1
            other.value < this.value -> 1
            else -> 0
        }

    operator fun plus(money: Money): Money {
        if (currency != money.currency) {
            throw UnsupportedOperationException("Can't add money values of different currencies: $currency != ${money.currency}")
        }

        val sum = value + money.value
        return Money(currency, sum)
    }

    operator fun minus(money: Money): Money {
        if (currency != money.currency) {
            throw UnsupportedOperationException("Can't subtract money values of different currencies: $currency != ${money.currency}")
        }

        val diff = value - money.value
        return Money(currency, diff)
    }

    fun negate(): Money = Money(currency, -value)

    override fun toString() = toString(defaultLocale)

    fun toString(locale: Locale): String {
        val fraction = valueToFraction(currency, value)

        val format =
            NumberFormat.getCurrencyInstance(locale.accountNumberFormat).apply {
                this.currency = this@Money.currency
            }

        return format.format(fraction)
    }

    fun toStringWithoutCurrency(locale: Locale = defaultLocale): String {
        val fraction = valueToFraction(currency, value)

        val format =
            NumberFormat.getInstance(locale).apply {
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

val Money.isZero: Boolean
    get() = value == 0L

val Money.isNegative: Boolean
    get() = value < 0L

val Money.isPositive: Boolean
    get() = value > 0L

private val Locale.accountNumberFormat: Locale
    get() =
        Locale.Builder()
            .setLocale(this)
            .setExtension(Locale.UNICODE_LOCALE_EXTENSION, "cf-account")
            .build()
