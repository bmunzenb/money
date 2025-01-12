package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXPayee
import com.munzenberger.money.app.model.getAllWithLastPaid
import com.munzenberger.money.app.observable.CompositeSubscription
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Payee

class PayeeListViewModel : AutoCloseable {
    private val payees = SimpleAsyncObjectProperty<List<FXPayee>>()

    val payeesProperty: ReadOnlyAsyncObjectProperty<List<FXPayee>> = payees

    private val subscriptions = CompositeSubscription()

    fun start(database: ObservableMoneyDatabase) {
        database.subscribe {
            payees.setValueAsync {
                Payee.getAllWithLastPaid(database).map { FXPayee(it.first, it.second) }
            }
        }.also { subscriptions.add(it) }
    }

    override fun close() {
        subscriptions.cancel()
    }
}
