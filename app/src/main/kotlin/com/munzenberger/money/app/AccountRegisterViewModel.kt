package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXTransactionDetail
import com.munzenberger.money.app.model.getTransactionDetails
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.rx.observableAccount
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class AccountRegisterViewModel : AutoCloseable {

    private data class SubscriptionResult(
            val account: Account,
            val transactions: List<FXTransactionDetail>,
            val endingBalance: Money
    )

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()
    private val transactions = SimpleAsyncObjectProperty<List<FXTransactionDetail>>()
    private val endingBalance = SimpleAsyncObjectProperty<Money>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXTransactionDetail>> = transactions
    val endingBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = endingBalance

    fun start(database: ObservableMoneyDatabase, accountIdentity: Long) {

        Account.observableAccount(accountIdentity, database)
                .flatMap { Observable.fromCallable {
                    SubscriptionResult(
                            account = it,
                            transactions = it.getTransactionDetails(database),
                            endingBalance = it.balance(database)) }
                }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe({
                    account.value = AsyncObject.Complete(it.account)
                    transactions.value = AsyncObject.Complete(it.transactions)
                    endingBalance.value = AsyncObject.Complete(it.endingBalance)
                }, {
                    account.value = AsyncObject.Error(it)
                    transactions.value = AsyncObject.Error(it)
                    endingBalance.value = AsyncObject.Error(it)
                })
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
