package com.munzenberger.money.app

import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransactionDetail
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

open class TransactionDetailEditor() {

    constructor(
            detail: TransactionDetail,
            categories: List<TransactionCategory>,
            type: TransactionType? = null
    ) : this() {

        when (detail) {

            is TransactionDetail.Transfer -> {
                category = categories.filterIsInstance<TransactionCategory.Transfer>().firstOrNull {
                    it.account == detail.transfer.account
                }
                amount = detail.transfer.amount?.forTransactionType(type)
                number = detail.transfer.number
                memo = detail.transfer.memo
            }

            is TransactionDetail.Entry -> {
                category = categories.filterIsInstance<TransactionCategory.Entry>().firstOrNull {
                    it.category == detail.entry.category
                }
                amount = detail.entry.amount?.forTransactionType(type)
                memo = detail.entry.memo
            }
        }
    }

    val selectedCategoryProperty = SimpleObjectProperty<TransactionCategory>()
    val amountProperty = SimpleObjectProperty<Money>()
    val numberProperty = SimpleStringProperty()
    val memoProperty = SimpleStringProperty()

    private val editorValidProperty = SimpleBooleanProperty(false).apply {
        bind(selectedCategoryProperty.isNotNull.and(amountProperty.isNotNull))
    }

    var category: TransactionCategory?
        get() = selectedCategoryProperty.value
        set(value) { selectedCategoryProperty.value = value }

    var amount: Money?
        get() = amountProperty.value
        set(value) { amountProperty.value = value }

    var number: String?
        get() = numberProperty.value
        set(value) { numberProperty.value = value }

    var memo: String?
        get() = memoProperty.value
        set(value) { memoProperty.value = value }

    val isEditorValid: Boolean
        get() = editorValidProperty.value

    fun copy() = TransactionDetailEditor().apply {
        this.category = this@TransactionDetailEditor.category
        this.amount = this@TransactionDetailEditor.amount
        this.number = this@TransactionDetailEditor.number
        this.memo = this@TransactionDetailEditor.memo
    }
}
