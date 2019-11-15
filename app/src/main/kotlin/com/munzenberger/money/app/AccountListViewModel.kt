package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class AccountListViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Money>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = totalBalance

    init {

        totalBalance.bindAsync(accountsProperty) { list ->

            val balanceObservables = list.map { it.balanceObservable }

            val totalObservable = when {
                balanceObservables.isEmpty() -> Single.just(Money.ZERO)
                else -> Single.zip(balanceObservables) {
                    it.fold(Money.ZERO) { acc, b -> acc.add(b as Money) }
                }
            }

            subscribeTo(totalObservable).also { disposables.add(it) }
        }
    }

    fun start(database: MoneyDatabase, schedulers: SchedulerProvider = SchedulerProvider.Default) {

        val getAccounts = FXAccount.getAssetsAndLiabilities(database)

        accounts.subscribeTo(getAccounts)

        database.updateObservable
                .observeOn(schedulers.main)
                .subscribe { accounts.subscribeTo(getAccounts) }
                .also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}
