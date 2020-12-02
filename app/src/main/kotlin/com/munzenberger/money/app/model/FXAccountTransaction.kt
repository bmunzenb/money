package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountTransaction
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.isNegative
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.LocalDate

class FXAccountTransaction(accountTransaction: AccountTransaction) {

    internal val transactionId = accountTransaction.transactionId

    val dateProperty: ReadOnlyObjectProperty<LocalDate> = SimpleObjectProperty(accountTransaction.date)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(accountTransaction.number)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountTransaction.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountTransaction.payee)

    val categoryProperty: ReadOnlyStringProperty
    val statusProperty: ReadOnlyObjectProperty<TransactionStatus>
    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    init {

        val category = when (accountTransaction.categories.size) {
            0 -> null
            1 -> accountTransaction.categories[0].getCategoryName()
            else -> SPLIT_CATEGORY_NAME
        }

        categoryProperty = SimpleStringProperty(category)

        statusProperty = SimpleObjectProperty(accountTransaction.status)

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

private fun AccountTransaction.Category.getCategoryName() = buildCategoryName(
        accountTypeCategory = accountTypeCategory,
        accountName = accountName,
        categoryName = categoryName
)

