package com.munzenberger.money.app.model

import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.isNegative
import io.reactivex.Single
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.util.Date

class FXAccountTransaction(private val accountTransaction: AccountTransaction, private val database: MoneyDatabase) {

    val dateProperty: ReadOnlyObjectProperty<Date> = SimpleObjectProperty(accountTransaction.date)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountTransaction.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountTransaction.payee)

    val categoryProperty: ReadOnlyStringProperty
    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    val getTransaction: Single<Transaction>
        get() = Single.fromCallable {
            Transaction.get(accountTransaction.transactionId, database)
                    ?: throw PersistableNotFoundException(Transaction::class, accountTransaction.transactionId)
        }

    init {

        categoryProperty = SimpleStringProperty().apply {
            value = when (accountTransaction.categories.size) {
                0 -> null
                1 -> accountTransaction.categories[0]
                else -> SPLIT_CATEGORY_NAME
            }
        }

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
