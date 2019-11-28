package com.munzenberger.money.app.control

import com.munzenberger.money.core.Money
import java.text.ParseException

class MoneyStringConverter : BlockStringConverter<Money>(
        Money::toStringWithoutCurrency,
        { value ->
            try {
                Money.valueOfFraction(value)
            } catch (e: ParseException) {
                null
            }
        })