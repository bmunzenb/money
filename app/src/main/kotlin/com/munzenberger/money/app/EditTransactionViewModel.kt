package com.munzenberger.money.app

import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import javafx.beans.property.SimpleObjectProperty

class EditTransactionViewModel {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee>()

    fun start(database: MoneyDatabase, account: Account) {

        selectedAccountProperty.set(account)
        accounts.subscribeTo(Account.getAll(database))
        payees.subscribeTo(Payee.getAll(database))
    }
}
