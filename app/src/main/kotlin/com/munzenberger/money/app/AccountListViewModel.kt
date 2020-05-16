package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.model.getAssetsAndLiabilities
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.app.property.subscribe
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class AccountListViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Money>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = totalBalance

    init {

        totalBalance.bindAsync(accountsProperty) { accounts ->

            val observableBalances = accounts.map { it.observableBalance }

            val observableTotal = when (observableBalances.isEmpty()) {
                true -> Single.just(Money.zero())
                else -> Single.zip(observableBalances) { it.fold(Money.zero()) { acc, b -> acc.add(b as Money) } }
            }

            value = AsyncObject.Executing()

            observableTotal.subscribeOn(SchedulerProvider.database)
                    .observeOn(SchedulerProvider.main)
                    .subscribe(this)
        }
    }

    fun start(database: ObservableMoneyDatabase) {

        database.onUpdate.flatMapAsyncObject { Account.getAssetsAndLiabilities(database).map { FXAccount(it, database) } }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { accounts.value = it }
                .also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}
