package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXRegisterEntry
import com.munzenberger.money.app.model.FXRegisterEntryFilter
import com.munzenberger.money.app.model.inCurrentMonth
import com.munzenberger.money.app.model.inCurrentYear
import com.munzenberger.money.app.model.inLastMonths
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.flatMapAsyncObject
import com.munzenberger.money.app.property.map
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountEntry
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.getAccountEntries
import com.munzenberger.money.core.getBalance
import com.munzenberger.money.core.model.AccountTypeGroup
import com.munzenberger.money.core.model.EntryTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.DeleteQueryBuilder
import com.munzenberger.money.sql.inGroup
import com.munzenberger.money.sql.transaction
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javafx.beans.binding.Bindings
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
            val transactions: List<FXRegisterEntry>,
            val endingBalance: Money
    )

    private val disposables = CompositeDisposable()

    private val account = SimpleAsyncObjectProperty<Account>()
    private val transactions = SimpleAsyncObjectProperty<List<FXRegisterEntry>>()
    private val endingBalance = SimpleAsyncObjectProperty<Money>()
    private val debitText = SimpleStringProperty()
    private val creditText = SimpleStringProperty()
    private val activeFilters = SimpleObjectProperty<Predicate<FXRegisterEntry>>()
    private val operationInProgress = SimpleBooleanProperty(false)

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXRegisterEntry>> = transactions
    val endingBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = endingBalance
    val debitTextProperty: ReadOnlyStringProperty = debitText
    val creditTextProperty: ReadOnlyStringProperty = creditText
    val dateFiltersProperty: ReadOnlyListProperty<FXRegisterEntryFilter>
    val statusFiltersProperty: ReadOnlyListProperty<FXRegisterEntryFilter>
    val activeFiltersProperty: ReadOnlyObjectProperty<Predicate<FXRegisterEntry>> = activeFilters
    val operationInProgressProperty: ReadOnlyBooleanProperty = operationInProgress

    val selectedDateFilterProperty = SimpleObjectProperty<FXRegisterEntryFilter>()
    val selectedStatusFilterProperty = SimpleObjectProperty<FXRegisterEntryFilter>()

    lateinit var database: MoneyDatabase

    init {

        val dateFilters = FXCollections.observableArrayList<FXRegisterEntryFilter>().apply {
            addAll(
                    FXRegisterEntryFilter("All Dates") { true },
                    FXRegisterEntryFilter("Current Month") { it.dateProperty.value.inCurrentMonth() },
                    FXRegisterEntryFilter("Current Year") { it.dateProperty.value.inCurrentYear() },
                    FXRegisterEntryFilter("Last 3 Months") { it.dateProperty.value.inLastMonths(3) },
                    FXRegisterEntryFilter("Last 12 Months") { it.dateProperty.value.inLastMonths(12) }
            )
        }

        dateFiltersProperty = SimpleListProperty(dateFilters)

        selectedDateFilterProperty.value = dateFilters[0]

        val statusFilters = FXCollections.observableArrayList<FXRegisterEntryFilter>().apply {
            addAll(
                    FXRegisterEntryFilter("All Transactions") { true },
                    FXRegisterEntryFilter("Unreconciled Transactions") { it.statusProperty.value != TransactionStatus.RECONCILED }
            )
        }

        statusFiltersProperty = SimpleListProperty(statusFilters)

        selectedStatusFilterProperty.value = statusFilters[0]

        val filtersBinding = Bindings.createObjectBinding(
                { selectedDateFilterProperty.value.and(selectedStatusFilterProperty.value) },
                selectedDateFilterProperty,
                selectedStatusFilterProperty
        )

        activeFilters.bind(filtersBinding)

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

            var transactions = account.getAccountEntries(database)
            var endingBalance = account.getBalance(database)

            if (account.accountType?.group == AccountTypeGroup.LIABILITIES) {
                transactions = transactions.map { it.negateBalance() }
                endingBalance = endingBalance.negate()
            }

            SubscriptionResult(
                    account = account,
                    transactions = transactions.map { FXRegisterEntry(it) },
                    endingBalance = endingBalance
            )
        }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe { async ->
                    account.value = async.map { it.account }
                    transactions.value = async.map { it.transactions }
                    endingBalance.value = async.map { it.endingBalance }
                }
                .also { disposables.add(it) }
    }

    fun getTransaction(transaction: FXRegisterEntry, block: (Transaction?, Throwable?) -> Unit) {

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

    fun deleteTransactions(transactions: List<FXRegisterEntry>, block: (Throwable?) -> Unit) {

        val ids = transactions.map { it.transactionId }

        Single.fromCallable {
            database.transaction { tx ->

                val deleteTransfers = DeleteQueryBuilder(TransferTable.name)
                        .where(TransferTable.transactionColumn.inGroup(ids))
                        .build()

                tx.executeUpdate(deleteTransfers)

                val deleteEntries = DeleteQueryBuilder(EntryTable.name)
                        .where(EntryTable.transactionColumn.inGroup(ids))
                        .build()

                tx.executeUpdate(deleteEntries)

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

    fun updateTransactionStatus(transaction: FXRegisterEntry, status: TransactionStatus, block: (Throwable?) -> Unit) {

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

private fun AccountEntry.negateBalance(): AccountEntry {
    return when (this) {
        is AccountEntry.Transaction -> copy(balance = balance.negate())
        is AccountEntry.Transfer -> copy(balance = balance.negate())
    }
}
