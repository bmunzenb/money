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

class EditTransfersViewModel {

    private val transfers = FXCollections.observableArrayList<EditTransfer> { t -> arrayOf(t.selectedCategoryProperty, t.amountProperty) }
    private val categories = FXCollections.observableArrayList<TransactionCategory>()

    private val doneDisabled = SimpleBooleanProperty(true)
    private val total = SimpleObjectProperty<Money>()

    val transfersProperty: ReadOnlyListProperty<EditTransfer> = SimpleListProperty(transfers)
    val categoriesProperty: ReadOnlyListProperty<TransactionCategory> = SimpleListProperty(categories)
    val doneDisabledProperty: ReadOnlyBooleanProperty = doneDisabled
    val totalProperty: ReadOnlyObjectProperty<Money> = total

    private lateinit var originalTransfers: ObservableList<EditTransfer>

    init {
        transfers.addListener(ListChangeListener {
            doneDisabled.value = it.list.isEmpty() || it.list.any { e -> !e.isValid }
            total.value = it.list.fold(Money.zero()) { acc, m -> acc.add(m.amount ?: Money.zero()) }
        })
    }

    fun start(transfers: ObservableList<EditTransfer>, categories: List<TransactionCategory>) {

        this.total.value = Money.zero()

        this.originalTransfers = transfers

        // operate on a copy of the original transfers and
        // apply the changes when the user taps Done

        val copied = transfers
                .filter { it.category != null || it.amount != null }
                .map { EditTransfer.from(it) }

        this.transfers.addAll(copied)

        this.categories.addAll(categories)
    }

    fun add(): Int {

        transfers.add(EditTransfer())

        return transfers.size - 1
    }

    fun delete(items: List<EditTransfer>) {

        transfers.removeAll(items)
    }

    fun done() {

        originalTransfers.setAll(transfers)
    }
}
