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

    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    init {
        when {
            accountTransaction.amount.isNegative -> {
                debitProperty = SimpleObjectProperty(accountTransaction.amount.negate())
                creditProperty = SimpleObjectProperty()
            }
            else -> {
                debitProperty = SimpleObjectProperty()
                creditProperty = SimpleObjectProperty(accountTransaction.amount)
            }
        }
    }
}
