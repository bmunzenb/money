package com.munzenberger.money.app

import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.core.CategoryEntry
import com.munzenberger.money.core.Entry
import com.munzenberger.money.core.EntryIdentity
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransferEntry
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

open class TransactionEntryEditor() {

    constructor(
            entry: Entry<out EntryIdentity>,
            categories: List<TransactionCategory>,
            type: TransactionType? = null
    ) : this() {

        this._entry = entry

        when (entry) {

            is TransferEntry -> {
                category = categories.filterIsInstance<TransactionCategory.TransferType>().firstOrNull {
                    it.account == entry.account
                }
                amount = entry.amount?.forTransactionType(type)
                memo = entry.memo
            }

            is CategoryEntry -> {
                category = categories.filterIsInstance<TransactionCategory.CategoryType>().firstOrNull {
                    it.category == entry.category
                }
                amount = entry.amount?.forTransactionType(type)
                memo = entry.memo
            }
        }
    }

    private var _entry: Entry<out EntryIdentity>? = null

    val entry: Entry<out EntryIdentity>?
        get() = _entry

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

    fun copy() = TransactionEntryEditor().apply {
        this._entry = this@TransactionEntryEditor.entry
        this.category = this@TransactionEntryEditor.category
        this.amount = this@TransactionEntryEditor.amount
        this.memo = this@TransactionEntryEditor.memo
    }
}
