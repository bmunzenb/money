package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

class BalanceAccountViewModel {

    private val transactions = SimpleAsyncObjectProperty<List<FXAccountEntry>>()
    private val debitText = SimpleStringProperty()
    private val creditText = SimpleStringProperty()

    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountEntry>> = transactions
    val debitTextProperty: ReadOnlyStringProperty = debitText
    val creditTextProperty: ReadOnlyStringProperty = creditText

    fun start(
            transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountEntry>>,
            debitTextProperty: ReadOnlyStringProperty,
            creditTextProperty: ReadOnlyStringProperty
    ) {
        this.transactions.bind(transactionsProperty)
        this.debitText.bind(debitTextProperty)
        this.creditText.bind(creditTextProperty)
    }
}
