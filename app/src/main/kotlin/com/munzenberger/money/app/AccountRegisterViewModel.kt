package com.munzenberger.money.app

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

        account.subscribe(single)

        val disposable = database.updateObservable
                .observeOn(JavaFxScheduler.platform())
                .subscribe { account.subscribe(single) }

        disposables.add(disposable)
    }

    override fun close() {
        disposables.clear()
    }
}
