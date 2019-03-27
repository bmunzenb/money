package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.beans.value.ChangeListener

class AccountListViewModel : AutoCloseable {

    private val disposables = CompositeDisposable()
    private val retainListeners = mutableListOf<ChangeListener<*>>()

    private val accounts = SimpleAsyncObjectProperty<List<FXAccount>>()
    private val totalBalance = SimpleAsyncObjectProperty<Money>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<FXAccount>> = accounts
    val totalBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = totalBalance

    init {

        retainListeners += totalBalance.bindAsync(accountsProperty) { list ->

            val balances = list.map { it.balanceObservable }

            val total = when {
                balances.isEmpty() -> Single.just(Money.ZERO)
                else -> Single.zip(balances) {
                    it.fold(Money.ZERO) { acc, b -> acc.add(b as Money) }
                }
            }

            subscribe(total)
        }
    }

    fun start(database: MoneyDatabase) {

        val getAccounts = Account.getAll(database).map {
            it.map { a -> FXAccount(a, database) }
        }

        accounts.subscribe(getAccounts)

        val disposable = database.updateObservable
                .observeOn(JavaFxScheduler.platform())
                .subscribe {
                    accounts.subscribe(getAccounts)
                }

        disposables.add(disposable)
    }

    override fun close() {
        disposables.clear()
    }
}
