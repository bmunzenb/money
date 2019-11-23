package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.rx.observableGet
import io.reactivex.disposables.CompositeDisposable

class AccountRegisterViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account

    fun start(database: ObservableMoneyDatabase, accountIdentity: Long, schedulers: SchedulerProvider = SchedulerProvider.Default) {

        val single = Account.observableGet(accountIdentity, database)

        account.subscribeTo(single)

        database.updateObservable
                .observeOn(schedulers.main)
                .subscribe { account.subscribeTo(single) }
                .also { disposables.add(it) }
    }

    fun getAccount(block: (Account) -> Unit) = account.get().let {
        when (it) {
            is AsyncObject.Complete -> block.invoke(it.value)
            else -> {}
        }
    }

    override fun close() {
        disposables.clear()
    }
}
