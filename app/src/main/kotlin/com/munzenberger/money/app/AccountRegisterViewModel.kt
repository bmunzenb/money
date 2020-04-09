package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccountTransaction
import com.munzenberger.money.app.model.FXAccountTransactionFilter
import com.munzenberger.money.app.model.getAccountTransactions
import com.munzenberger.money.app.model.inCurrentMonth
import com.munzenberger.money.app.model.inCurrentYear
import com.munzenberger.money.app.model.inLastMonths
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
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import java.util.function.Predicate

class AccountRegisterViewModel : AutoCloseable {

    private data class SubscriptionResult(
            val account: Account,
            val transactions: List<FXAccountTransaction>,
            val endingBalance: Money
    )

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()
    private val transactions = SimpleAsyncObjectProperty<List<FXAccountTransaction>>()
    private val endingBalance = SimpleAsyncObjectProperty<Money>()
    private val debitText = SimpleStringProperty()
    private val creditText = SimpleStringProperty()
    private val activeFilters = SimpleObjectProperty<Predicate<FXAccountTransaction>>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountTransaction>> = transactions
    val endingBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = endingBalance
    val debitTextProperty: ReadOnlyStringProperty = debitText
    val creditTextProperty: ReadOnlyStringProperty = creditText
    val dateFiltersProperty: ReadOnlyListProperty<FXAccountTransactionFilter>
    val activeFiltersProperty: ReadOnlyObjectProperty<Predicate<FXAccountTransaction>> = activeFilters

    val selectedDateFilterProperty = SimpleObjectProperty<FXAccountTransactionFilter>()

    init {

        val dateFilters = FXCollections.observableArrayList<FXAccountTransactionFilter>().apply {
            addAll(
                    FXAccountTransactionFilter("All Dates", Predicate { true }),
                    FXAccountTransactionFilter("Current Month", Predicate { it.dateProperty.value.inCurrentMonth() }),
                    FXAccountTransactionFilter("Current Year", Predicate { it.dateProperty.value.inCurrentYear() }),
                    FXAccountTransactionFilter("Last 3 Months", Predicate { it.dateProperty.value.inLastMonths(3) }),
                    FXAccountTransactionFilter("Last 12 Months", Predicate { it.dateProperty.value.inLastMonths(12) })
            )
        }

        dateFiltersProperty = SimpleListProperty(dateFilters)

        selectedDateFilterProperty.addListener { _, _, filter ->
            activeFilters.value = filter
        }

        account.addListener { _, _, newValue ->
            when (newValue) {
                is AsyncObject.Complete -> newValue.value.accountType?.run {
                    TransactionType.getTypes(this).run {
                        debitText.value = find { it.variant == TransactionType.Variant.DEBIT }?.name
                        creditText.value = find { it.variant == TransactionType.Variant.CREDIT }?.name
                    }
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

                        var transactions = it.getAccountTransactions(database)
                        var endingBalance = it.balance(database)

                        if (it.accountType!!.category == AccountType.Category.LIABILITIES) {
                            transactions = transactions.map { it.copy(balance = it.balance.negate()) }
                            endingBalance = endingBalance.negate()
                        }

                        SubscriptionResult(
                                account = it,
                                transactions = transactions.map { FXAccountTransaction(it) },
                                endingBalance = endingBalance
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

    override fun close() {
        disposables.clear()
    }
}
