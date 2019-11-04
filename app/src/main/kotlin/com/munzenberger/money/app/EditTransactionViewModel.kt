package com.munzenberger.money.app

import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.model.PayeeTable
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import java.time.LocalDate

class EditTransactionViewModel {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val selectedTypeProperty = SimpleObjectProperty<TransactionType?>()
    val date = SimpleObjectProperty<LocalDate>()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()

    fun start(database: MoneyDatabase, account: Account) {

        selectedAccountProperty.set(account)
        accounts.subscribeTo(Account.getAll(database))
        types.addAll(TransactionType.getTypes())
        date.set(LocalDate.now())
        payees.subscribeTo(Payee.getAll(database, PayeeTable.nameColumn))
    }
}
