package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.model.observableAssetsAndLiabilities
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.subscribe
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class AccountListViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Money>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = totalBalance

    init {

        totalBalance.bindAsync(accountsProperty) { list ->

            val observableBalances = list.map { it.observableBalance }

            val observableTotal = when {
                observableBalances.isEmpty() -> Observable.just(Money.zero())
                else -> Observable.zip(observableBalances) {
                    it.fold(Money.zero()) { acc, b -> acc.add(b as Money) }
                }
            }

            val schedulers = SchedulerProvider.Default

            observableTotal.subscribeOn(schedulers.database)
                    .observeOn(schedulers.main)
                    .subscribe(totalBalance)
                    .also { disposables.add(it) }
        }
    }

    fun start(database: ObservableMoneyDatabase, schedulers: SchedulerProvider = SchedulerProvider.Default) {

        Account.observableAssetsAndLiabilities(database)
                .subscribeOn(schedulers.database)
                .map { it.map { a -> FXAccount(a, database) } }
                .observeOn(schedulers.main)
                .subscribe(accounts)
                .also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}
