package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXPayee
import com.munzenberger.money.app.model.getAllWithLastPaid
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import io.reactivex.disposables.CompositeDisposable

class PayeeListViewModel : AutoCloseable {

    private val payees = SimpleAsyncObjectProperty<List<FXPayee>>()

    val payeesProperty: ReadOnlyAsyncObjectProperty<List<FXPayee>> = payees

    private val disposables = CompositeDisposable()

    fun start(database: ObservableMoneyDatabase, schedulers: SchedulerProvider = SchedulerProvider.Default) {

        val getPayees = Payee.getAllWithLastPaid(database)
                .map { it.map { p -> FXPayee(p.first, p.second) } }

        payees.subscribeTo(getPayees, schedulers)

        database.updateObservable
                .observeOn(schedulers.main)
                .subscribe { payees.subscribeTo(getPayees, schedulers) }
                .also { disposables.add(it) }
    }

    override fun close() {
        disposables.clear()
    }
}
