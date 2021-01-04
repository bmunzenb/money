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

        this._detail = detail

        when (detail) {

            is TransactionDetail.Transfer -> {
                category = categories.filterIsInstance<TransactionCategory.Transfer>().firstOrNull {
                    it.account == detail.transfer.account
                }
                amount = detail.transfer.amount?.forTransactionType(type)
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

    private var _detail: TransactionDetail? = null

    val detail: TransactionDetail?
        get() = _detail

    val selectedCategoryProperty = SimpleObjectProperty<TransactionCategory>()
    val amountProperty = SimpleObjectProperty<Money>()
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

    var memo: String?
        get() = memoProperty.value
        set(value) { memoProperty.value = value }

    val isEditorValid: Boolean
        get() = editorValidProperty.value

    fun copy() = TransactionDetailEditor().apply {
        this._detail = this@TransactionDetailEditor.detail
        this.category = this@TransactionDetailEditor.category
        this.amount = this@TransactionDetailEditor.amount
        this.memo = this@TransactionDetailEditor.memo
    }
}
