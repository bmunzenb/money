package com.munzenberger.money.app.control

import com.munzenberger.money.core.Money
import java.text.ParseException

class MoneyStringConverter : BlockStringConverter<Money>(
        { money -> money.toStringWithoutCurrency() },
        { value ->
            try {
                Money.valueOfFraction(value)
            } catch (e: ParseException) {
                null
            }
        })
