package com.munzenberger.money.app

import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import java.time.LocalDate

class EditTransferViewModel {

    private val types = FXCollections.observableArrayList<TransactionType>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val saveStatus = SimpleAsyncStatusProperty()
    private val transactionStatus = SimpleStringProperty()
    private val notValid = SimpleBooleanProperty()

    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val selectedTypeProperty = SimpleObjectProperty<TransactionType>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val numberProperty = SimpleStringProperty()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus
    val transactionStatusProperty: ReadOnlyStringProperty = transactionStatus
    val notValidProperty: BooleanProperty = notValid

    private lateinit var database: MoneyDatabase

    init {
        notValid.value = true
    }

    fun start(database: MoneyDatabase, transferId: Long) {

    }

    fun save() {

    }
}
