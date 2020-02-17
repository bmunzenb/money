package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.subscribe
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.rx.observableAccount
import io.reactivex.disposables.CompositeDisposable

class AccountRegisterViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account

    fun start(database: ObservableMoneyDatabase, accountIdentity: Long) {

        Account.observableAccount(accountIdentity, database)
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe(account)
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
