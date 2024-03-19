package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.observable.CompositeSubscription
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.combineWith
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import javafx.beans.value.ChangeListener

class AccountListViewModel : AutoCloseable {

    private val subscriptions = CompositeSubscription()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Money>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = totalBalance

    init {

        accountsProperty.addListener { _, _, accts ->
            when (accts) {
                is AsyncObject.Complete -> {

                    val balanceProperties = accts.value.map { it.balanceProperty }

                    when (balanceProperties.isEmpty()) {
                        true -> totalBalance.value = AsyncObject.Pending()
                        else -> {
                            val balancePropertyListener = ChangeListener<AsyncObject<Money>> { _, _, _ ->
                                val initialValue: AsyncObject<Money> = AsyncObject.Complete(Money.ZERO)
                                totalBalance.value = balanceProperties.fold(initialValue) { acc, b -> acc + b.value }
                            }

                            balanceProperties.forEach { it.addListener(balancePropertyListener) }

                            accts.value.forEach { it.fetchBalance() }
                        }
                    }
                }
                else -> totalBalance.value = AsyncObject.Pending()
            }
        }
    }

    fun start(database: ObservableMoneyDatabase) {
        database.subscribe {
            accounts.setValueAsync { Account.getAll(database).map { FXAccount(it, database) } }
        }.also { subscriptions.add(it) }
    }

    override fun close() {
        subscriptions.cancel()
    }
}

private operator fun AsyncObject<Money>.plus(other: AsyncObject<Money>): AsyncObject<Money> =
        combineWith(other) { m1, m2 -> m1 + m2 }
