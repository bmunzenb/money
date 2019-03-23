package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler

class AccountListViewModel {

    private val disposables = CompositeDisposable()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Long>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Long> = totalBalance

    fun start(database: MoneyDatabase) {

        totalBalance.bindAsync(accountsProperty) { list ->

            val balances = list.map { it.balanceObservable }

            val total = Single.zip(balances) {
                it.fold(0L) { acc, b -> acc + (b as Long) }
            }

            subscribe(total)
        }

        val getAccounts = Account.getAll(database).map {
            it.map { a -> FXAccount(a, database) }
        }

        accounts.subscribe(getAccounts)

        val disposable = database.updateObservable
                .observeOn(JavaFxScheduler.platform())
                .subscribe {
                    accounts.subscribe(getAccounts)
                }

        disposables.add(disposable)
    }

    fun clear() {
        disposables.clear()
    }
}
