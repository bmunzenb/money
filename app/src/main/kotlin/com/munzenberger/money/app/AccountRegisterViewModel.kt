package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXTransactionDetail
import com.munzenberger.money.app.model.getTransactionDetails
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import com.munzenberger.money.core.rx.observableAccount
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleStringProperty

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
    private val debitText = SimpleStringProperty()
    private val creditText = SimpleStringProperty()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXTransactionDetail>> = transactions
    val endingBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = endingBalance
    val debitTextProperty: ReadOnlyStringProperty = debitText
    val creditTextProperty: ReadOnlyStringProperty = creditText

    init {
        account.addListener { _, _, newValue ->
            when (newValue) {
                is AsyncObject.Complete -> newValue.value.accountType?.run {
                    debitText.value = TransactionType.findType(this, TransactionType.Variant.DEBIT)?.name
                    creditText.value = TransactionType.findType(this, TransactionType.Variant.CREDIT)?.name
                }
                else -> {
                    // don't bother clearing the titles as we don't want flicker
                }
            }
        }
    }

    fun start(database: ObservableMoneyDatabase, accountIdentity: Long) {

        Account.observableAccount(accountIdentity, database)
                .flatMap {
                    Observable.fromCallable {

                        val invertBalance = it.accountType!!.category == AccountType.Category.LIABILITIES

                        SubscriptionResult(
                                account = it,
                                transactions = it.getTransactionDetails(database, invertBalance),
                                endingBalance = it.balance(database).let { if (invertBalance) it.negate() else it }
                        )
                    }
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
