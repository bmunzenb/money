package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXPayee
import com.munzenberger.money.app.model.getAllWithLastPaid
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Payee
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PayeeListViewModel : AutoCloseable {

    private val payees = SimpleAsyncObjectProperty<List<FXPayee>>()

    val payeesProperty: ReadOnlyAsyncObjectProperty<List<FXPayee>> = payees

    private val disposables = CompositeDisposable()

    fun start(database: ObservableMoneyDatabase) {
        database.onUpdate.subscribe {
            payees.setValueAsync {
                Payee.getAllWithLastPaid(database).map { FXPayee(it.first, it.second) }
            }
        }.also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}
