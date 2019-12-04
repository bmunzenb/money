package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.util.Callback

class EditTransfersViewModel : EditTransferBase() {

    private val transfers = FXCollections.observableArrayList<EditTransfer> { t -> arrayOf(t.selectedCategoryProperty, t.amountProperty) }
    private val categories = FXCollections.observableArrayList<DelayedCategory>()

    private val addDisabled = SimpleBooleanProperty(true).apply {
        bind(selectedCategoryProperty.isNull.or(amountProperty.isNull))
    }

    private val doneDisabled = SimpleBooleanProperty(true)

    val transfersProperty: ReadOnlyListProperty<EditTransfer> = SimpleListProperty(transfers)
    val categoriesProperty: ReadOnlyListProperty<DelayedCategory> = SimpleListProperty(categories)
    val addDisabledProperty: ReadOnlyBooleanProperty = addDisabled
    val doneDisabledProperty: ReadOnlyBooleanProperty = doneDisabled

    private lateinit var originalTransfers: ObservableList<EditTransfer>

    init {
        transfers.addListener(ListChangeListener {
            doneDisabled.value = it.list.isEmpty() || it.list.any { e -> !e.isValid }
        })
    }

    fun start(transfers: ObservableList<EditTransfer>, categories: List<DelayedCategory>) {

        this.originalTransfers = transfers

        // operate on a copy of the original transfers and
        // apply the changes when the user taps Done

        val copied = transfers
                .filter { it.category != null || it.amount != null }
                .map { EditTransfer.from(it) }

        this.transfers.addAll(copied)

        this.categories.addAll(categories)
    }

    fun add() {

        val editTransfer = EditTransfer()
        editTransfer.category = category
        editTransfer.amount = amount
        editTransfer.memo = memo

        transfers.add(editTransfer)

        category = null
        memo = null
        amount = null
    }

    fun delete(items: List<EditTransfer>) {

        transfers.removeAll(items)
    }

    fun done() {

        originalTransfers.setAll(transfers)
    }
}
