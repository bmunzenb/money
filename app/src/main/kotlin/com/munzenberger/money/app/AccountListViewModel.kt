package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.disposables.CompositeDisposable

class AccountListViewModel {

    private val disposables = CompositeDisposable()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts

    fun start(database: MoneyDatabase) {

        val single = Account.getAll(database).map {
            it.map { a -> FXAccount(a, database) }
        }

        accounts.subscribe(single)

        val disposable = database.updateObservable.subscribe {
            accounts.subscribe(single)
        }

        disposables.add(disposable)
    }

    fun clear() {
        disposables.clear()
    }
}
