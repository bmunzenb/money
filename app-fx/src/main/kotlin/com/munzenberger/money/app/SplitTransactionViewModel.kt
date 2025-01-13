package com.munzenberger.money.app

import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.core.Money
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

class SplitTransactionViewModel {
    private val editors =
        FXCollections.observableArrayList<TransactionEntryEditor> { e ->
            arrayOf(e.selectedCategoryProperty, e.amountProperty)
        }
    private val categories = FXCollections.observableArrayList<TransactionCategory>()

    private val doneDisabled = SimpleBooleanProperty(true)
    private val total = SimpleObjectProperty<Money>()

    val editorsProperty: ReadOnlyListProperty<TransactionEntryEditor> = SimpleListProperty(editors)
    val categoriesProperty: ReadOnlyListProperty<TransactionCategory> = SimpleListProperty(categories)
    val doneDisabledProperty: ReadOnlyBooleanProperty = doneDisabled
    val totalProperty: ReadOnlyObjectProperty<Money> = total

    private lateinit var originalEditors: ObservableList<TransactionEntryEditor>

    init {
        editors.addListener(
            ListChangeListener { change ->
                change.list.apply {
                    doneDisabled.value = isEmpty() || any { e -> !e.isEditorValid }
                    total.value = fold(Money.ZERO) { acc, m -> acc + (m.amount ?: Money.ZERO) }
                }
            },
        )
    }

    fun start(
        editors: ObservableList<TransactionEntryEditor>,
        categories: List<TransactionCategory>,
    ) {
        this.total.value = Money.ZERO

        this.originalEditors = editors

        // operate on a copy of the original transfers and
        // apply the changes when the user taps Done

        val copied =
            editors
                .filter { it.isEditorValid }
                .map { it.copy() }

        this.editors.addAll(copied)

        this.categories.addAll(categories)
    }

    fun add(): Int {
        editors.add(TransactionEntryEditor())

        return editors.size - 1
    }

    fun delete(items: List<TransactionEntryEditor>) {
        editors.removeAll(items)
    }

    fun moveUp(items: List<TransactionEntryEditor>): Int {
        // assuming only one item can be selected at a time, move the first
        items.firstOrNull()?.let {
            val index = editors.indexOf(it)
            if (index > 0) {
                editors.removeAt(index)
                val newIndex = index - 1
                editors.add(newIndex, it)
                return newIndex
            }
            return index
        }
        return -1
    }

    fun moveDown(items: List<TransactionEntryEditor>): Int {
        // assuming only one item can be selected at a time, move the first
        items.firstOrNull()?.let {
            val index = editors.indexOf(it)
            if (index < editors.size - 1) {
                editors.removeAt(index)
                val newIndex = index + 1
                editors.add(newIndex, it)
                return newIndex
            }
            return index
        }
        return -1
    }

    fun done() {
        originalEditors.setAll(editors)
    }
}
