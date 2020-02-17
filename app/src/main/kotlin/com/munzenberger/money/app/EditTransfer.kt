package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.Transfer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

abstract class EditTransferBase {

    val selectedCategoryProperty = SimpleObjectProperty<DelayedCategory>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()

    var category: DelayedCategory?
        get() = selectedCategoryProperty.value
        set(value) { selectedCategoryProperty.value = value }

    var amount: Money?
        get() = amountProperty.value
        set(value) { amountProperty.value = value }

    var memo: String?
        get() = memoProperty.value
        set(value) { memoProperty.value = value }
}

class EditTransfer : EditTransferBase() {

    companion object {

        fun from(transfer: Transfer, transactionType: TransactionType?) = EditTransfer().apply {
            category = transfer.category?.let { DelayedCategory.from(it) }
            amount = transfer.amount?.forTransactionType(transactionType)
            memo = transfer.memo
        }

        fun from(editTransfer: EditTransferBase) = EditTransfer().apply {
            category = editTransfer.category
            amount = editTransfer.amount
            memo = editTransfer.memo
        }
    }

    private val valid = SimpleBooleanProperty(false).apply {
        bind(selectedCategoryProperty.isNotNull.and(amountProperty.isNotNull))
    }

    fun getAmountValue(transactionType: TransactionType): Money {
        return amount!!.forTransactionType(transactionType)
    }

    val isValid: Boolean
        get() = valid.value
}