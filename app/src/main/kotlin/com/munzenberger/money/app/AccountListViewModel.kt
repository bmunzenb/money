package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase

class AccountListViewModel {

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts

    fun start(database: MoneyDatabase) {

        accounts.subscribe(Account.getAll(database).map {
            it.map { a -> FXAccount(a) }
        })
    }
}
