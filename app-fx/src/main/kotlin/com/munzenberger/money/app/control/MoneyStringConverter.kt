package com.munzenberger.money.app.control

import com.munzenberger.money.core.Money
import java.text.ParseException

class MoneyStringConverter :
    BlockStringConverter<Money>(
        { money -> money.toStringWithoutCurrency() },
        { value ->
            try {
                Money.valueOf(value)
            } catch (e: ParseException) {
                null
            }
        },
    )
