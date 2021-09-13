package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.map
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AccountListViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Money>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = totalBalance

    init {

        accountsProperty.addListener { _, _, newValue ->
            when (newValue) {
                is AsyncObject.Complete -> {

                    val observableBalances = newValue.value.map { it.singleBalance }

                    val observableTotal = when (observableBalances.isEmpty()) {
                        true -> Single.just(Money.ZERO)
                        else -> Single.zip(observableBalances) {
                            it.fold(Money.ZERO) { acc, b -> acc + b as Money }
                        }
                    }

                    totalBalance.value = AsyncObject.Executing()

                    observableTotal
                            .subscribeOn(SchedulerProvider.SINGLE)
                            .observeOn(SchedulerProvider.PLATFORM)
                            .subscribe(
                                    { totalBalance.value = AsyncObject.Complete(it) },
                                    { totalBalance.value = AsyncObject.Error(it) }
                            )
                            .also { disposables.add(it) }
                }
                else -> totalBalance.value = newValue.map { Money.ZERO }
            }
        }
    }

    fun start(database: ObservableMoneyDatabase) {
        database.onUpdate.subscribe {
            accounts.setValueAsync { Account.getAll(database).map { FXAccount(it, database) } }
        }.also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}
