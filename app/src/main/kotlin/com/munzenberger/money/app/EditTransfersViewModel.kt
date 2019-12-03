package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class EditTransfersViewModel : EditTransferBase() {

    private val transfers: ObservableList<EditTransfer> = FXCollections.observableArrayList()
    private val categories: ObservableList<DelayedCategory> = FXCollections.observableArrayList()
    private val addDisabled = SimpleBooleanProperty(true)
    private val doneDisabled = SimpleBooleanProperty(true)

    val transfersProperty: ReadOnlyListProperty<EditTransfer> = SimpleListProperty(transfers)
    val categoriesProperty: ReadOnlyListProperty<DelayedCategory> = SimpleListProperty(categories)
    val addDisabledProperty: ReadOnlyBooleanProperty = addDisabled
    val doneDisabledProperty: ReadOnlyBooleanProperty = doneDisabled

    private lateinit var originalTransfers: ObservableList<EditTransfer>

    init {
        addDisabled.bind(selectedCategoryProperty.isNull.or(amountProperty.isNull))
    }

    fun start(transfers: ObservableList<EditTransfer>, categories: List<DelayedCategory>) {

        this.originalTransfers = transfers

        // operate on a copy of the original transfers and
        // apply the changes when the user taps Done
        this.transfers.addAll(transfers.map { EditTransfer.from(it) })

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
}
