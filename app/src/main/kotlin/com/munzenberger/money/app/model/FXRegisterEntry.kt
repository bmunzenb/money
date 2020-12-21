package com.munzenberger.money.app.model

import com.munzenberger.money.core.Money
import com.munzenberger.money.core.RegisterEntry
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.sql.QueryExecutor
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.time.LocalDate

class FXRegisterEntry(private val registerEntry: RegisterEntry) {

    internal val transactionId = registerEntry.transactionId

    private val status = SimpleObjectProperty(registerEntry.status)

    val dateProperty: ReadOnlyObjectProperty<LocalDate> = SimpleObjectProperty(registerEntry.date)
    val numberProperty: ReadOnlyStringProperty = SimpleStringProperty(registerEntry.number)
    val balanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(registerEntry.balance)
    val payeeProperty: ReadOnlyStringProperty = SimpleStringProperty(registerEntry.payeeName)

    val categoryProperty: ReadOnlyStringProperty
    val statusProperty: ReadOnlyObjectProperty<TransactionStatus> = status
    val debitProperty: ReadOnlyObjectProperty<Money>
    val creditProperty: ReadOnlyObjectProperty<Money>

    init {

        val category = when (registerEntry.details.size) {
            0 -> null
            1 -> registerEntry.details[0].name
            else -> SPLIT_CATEGORY_NAME
        }

        categoryProperty = SimpleStringProperty(category)

        when {
            registerEntry.amount.isNegative -> {
                debitProperty = SimpleObjectProperty(registerEntry.amount.negate())
                creditProperty = SimpleObjectProperty()
            }
            else -> {
                debitProperty = SimpleObjectProperty()
                creditProperty = SimpleObjectProperty(registerEntry.amount)
            }
        }
    }

    fun updateStatus(status: TransactionStatus, executor: QueryExecutor) {
        registerEntry.updateStatus(status, executor)
        this.status.value = status
    }
}

private val RegisterEntry.Detail.name: String
    get() = when (this) {
        is RegisterEntry.Detail.Transfer ->
            "Transfer $CATEGORY_DELIMITER $accountName"
        is RegisterEntry.Detail.Entry ->
            when (val p = parentCategoryName) {
                null -> categoryName
                else -> "$p $CATEGORY_DELIMITER $categoryName"
            }
    }
