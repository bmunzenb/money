package com.munzenberger.money.app.model

import com.munzenberger.money.core.Money
import com.munzenberger.money.core.isNegative
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.util.Date

class FXAccountTransaction (accountTransaction: AccountTransaction) {

    val dateProperty: ReadOnlyObjectProperty<Date> = SimpleObjectProperty(accountTransaction.date)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountTransaction.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountTransaction.payee)

    val paymentProperty: ReadOnlyObjectProperty<Money>
    val depositProperty: ReadOnlyObjectProperty<Money>

    init {
        when {
            accountTransaction.amount.isNegative -> {
                paymentProperty = SimpleObjectProperty(accountTransaction.amount.negate())
                depositProperty = SimpleObjectProperty()
            }
            else -> {
                paymentProperty = SimpleObjectProperty()
                depositProperty = SimpleObjectProperty(accountTransaction.amount)
            }
        }
    }
}
