package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXAccountTransaction
import com.munzenberger.money.app.model.FXAccountTransactionFilter
import com.munzenberger.money.app.model.inCurrentMonth
import com.munzenberger.money.app.model.inCurrentYear
import com.munzenberger.money.app.model.inLastMonths
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.getRegister
import com.munzenberger.money.core.model.AccountTypeGroup
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.DeleteQueryBuilder
import com.munzenberger.money.sql.inGroup
import com.munzenberger.money.sql.transaction
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleBooleanProperty
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
    private val operationInProgress = SimpleBooleanProperty(false)

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountTransaction>> = transactions
    val endingBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = endingBalance
    val debitTextProperty: ReadOnlyStringProperty = debitText
    val creditTextProperty: ReadOnlyStringProperty = creditText
    val dateFiltersProperty: ReadOnlyListProperty<FXAccountTransactionFilter>
    val activeFiltersProperty: ReadOnlyObjectProperty<Predicate<FXAccountTransaction>> = activeFilters
    val operationInProgressProperty: ReadOnlyBooleanProperty = operationInProgress

    val selectedDateFilterProperty = SimpleObjectProperty<FXAccountTransactionFilter>()

    lateinit var database: MoneyDatabase

    init {

        val dateFilters = FXCollections.observableArrayList<FXAccountTransactionFilter>().apply {
            addAll(
                    FXAccountTransactionFilter("All Dates") { true },
                    FXAccountTransactionFilter("Current Month") { it.dateProperty.value.inCurrentMonth() },
                    FXAccountTransactionFilter("Current Year") { it.dateProperty.value.inCurrentYear() },
                    FXAccountTransactionFilter("Last 3 Months") { it.dateProperty.value.inLastMonths(3) },
                    FXAccountTransactionFilter("Last 12 Months") { it.dateProperty.value.inLastMonths(12) }
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

        this.database = database

        database.onUpdate.flatMapAsyncObject {

            val account = Account.get(accountIdentity, database)
                    ?: throw PersistableNotFoundException(Account::class, accountIdentity)

            var transactions = account.getRegister(database)
            var endingBalance = account.getBalance(database)

            if (account.accountType!!.group == AccountTypeGroup.LIABILITIES) {
                transactions = transactions.map { it.copy(balance = it.balance.negate()) }
                endingBalance = endingBalance.negate()
            }

            SubscriptionResult(
                    account = account,
                    transactions = transactions.map { FXAccountTransaction(it) },
                    endingBalance = endingBalance
            )
        }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe {
                    when (it) {
                        is AsyncObject.Pending -> {
                            account.value = AsyncObject.Pending()
                            transactions.value = AsyncObject.Pending()
                            endingBalance.value = AsyncObject.Pending()
                        }
                        is AsyncObject.Executing -> {
                            account.value = AsyncObject.Executing()
                            transactions.value = AsyncObject.Executing()
                            endingBalance.value = AsyncObject.Executing()
                        }
                        is AsyncObject.Complete -> {
                            account.value = AsyncObject.Complete(it.value.account)
                            transactions.value = AsyncObject.Complete(it.value.transactions)
                            endingBalance.value = AsyncObject.Complete(it.value.endingBalance)
                        }
                        is AsyncObject.Error -> {
                            account.value = AsyncObject.Error(it.error)
                            transactions.value = AsyncObject.Error(it.error)
                            endingBalance.value = AsyncObject.Error(it.error)
                        }
                    }
                }
                .also { disposables.add(it) }
    }

    fun getTransaction(transaction: FXAccountTransaction, block: (Transaction?, Throwable?) -> Unit) {

        Single.fromCallable {
            Transaction.get(transaction.transactionId, database)
                    ?: throw PersistableNotFoundException(Transaction::class, transaction.transactionId)
        }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe { t, error -> block.invoke(t, error) }
    }

    fun deleteTransactions(transactions: List<FXAccountTransaction>, block: (Throwable?) -> Unit) {

        val ids = transactions.map { it.transactionId }

        Single.fromCallable {
            database.transaction { tx ->

                val deleteTransfers = DeleteQueryBuilder(TransferTable.name)
                        .where(TransferTable.transactionColumn.inGroup(ids))
                        .build()

                tx.executeUpdate(deleteTransfers)

                val deleteTransactions = DeleteQueryBuilder(TransactionTable.name)
                        .where(TransactionTable.identityColumn.inGroup(ids))
                        .build()

                tx.executeUpdate(deleteTransactions)
            }
        }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe { _, error -> block.invoke(error) }
    }

    fun updateTransactionStatus(transaction: FXAccountTransaction, status: TransactionStatus, block: (Throwable?) -> Unit) {

        Single.fromCallable { transaction.updateStatus(status, database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe { _, error -> block.invoke(error) }
    }

    override fun close() {
        disposables.clear()
    }
}
