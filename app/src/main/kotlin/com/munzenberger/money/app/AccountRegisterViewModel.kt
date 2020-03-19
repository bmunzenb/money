package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXTransactionDetail
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.subscribe
import com.munzenberger.money.app.property.flatMap
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.rx.observableAccount
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class AccountRegisterViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()
    private val transactions = SimpleAsyncObjectProperty<List<FXTransactionDetail>>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXTransactionDetail>> = transactions

    fun start(database: ObservableMoneyDatabase, accountIdentity: Long) {

        Account.observableAccount(accountIdentity, database)
                .flatMap(account) { Observable.fromCallable { FXTransactionDetail.getTransactionsForAccount(it.identity!!, it.initialBalance!!, database) } }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe(transactions)
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
