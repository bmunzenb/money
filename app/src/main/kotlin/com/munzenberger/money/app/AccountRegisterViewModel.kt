package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler

class AccountRegisterViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account

    fun start(database: MoneyDatabase, accountIdentity: Long) {

        val single = Account.get(accountIdentity, database)

        account.subscribeTo(single)

        database.updateObservable
                .observeOn(JavaFxScheduler.platform())
                .subscribe { account.subscribeTo(single) }
                .also { disposables.add(it) }
    }

    fun getAccount(block: (Account) -> Unit) = account.get().let {
        when (it) {
            is AsyncObject.Complete -> block.invoke(it.value)
        }
    }

    override fun close() {
        disposables.clear()
    }
}
