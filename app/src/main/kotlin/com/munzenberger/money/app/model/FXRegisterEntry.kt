package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountEntry
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.sql.QueryExecutor
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.LocalDate

class FXRegisterEntry(private val accountEntry: AccountEntry) {

    internal val transactionId = accountEntry.transactionId

    private val status = SimpleObjectProperty(accountEntry.status)

    val dateProperty: ReadOnlyObjectProperty<LocalDate> = SimpleObjectProperty(accountEntry.date)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(accountEntry.number)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(accountEntry.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(accountEntry.payeeName)

    val categoryProperty: ReadOnlyStringProperty
    val statusProperty: ReadOnlyObjectProperty<TransactionStatus> = status
    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    init {

        val category = when (accountEntry) {
            is AccountEntry.Transaction -> when (accountEntry.details.size) {
                0 -> null
                1 -> accountEntry.details[0].name
                else -> SPLIT_CATEGORY_NAME
            }
            is AccountEntry.Transfer ->
                "Transfer $CATEGORY_DELIMITER ${accountEntry.transactionAccountName}"
        }

        categoryProperty = SimpleStringProperty(category)

        when {
            accountEntry.amount.isNegative -> {
                debitProperty = SimpleObjectProperty(accountEntry.amount.negate())
                creditProperty = SimpleObjectProperty()
            }
            else -> {
                debitProperty = SimpleObjectProperty()
                creditProperty = SimpleObjectProperty(accountEntry.amount)
            }
        }
    }

    fun updateStatus(status: TransactionStatus, executor: QueryExecutor) {
        accountEntry.updateStatus(status, executor)
        this.status.value = status
    }
}

private val AccountEntry.Transaction.Detail.name: String
    get() = when (this) {
        is AccountEntry.Transaction.Detail.Transfer ->
            "Transfer $CATEGORY_DELIMITER $accountName"
        is AccountEntry.Transaction.Detail.Entry ->
            when (val p = parentCategoryName) {
                null -> categoryName
                else -> "$p $CATEGORY_DELIMITER $categoryName"
            }
    }
