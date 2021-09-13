package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXRegisterEntry
import com.munzenberger.money.app.model.FXRegisterEntryFilter
import com.munzenberger.money.app.model.inCurrentMonth
import com.munzenberger.money.app.model.inCurrentYear
import com.munzenberger.money.app.model.inLastMonths
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsyncValue
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
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq
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

    sealed class Edit {
        data class Transaction(val transaction: com.munzenberger.money.core.Transaction) : Edit()
        data class Transfer(val transferId: Long) : Edit()
        data class Error(val error: Throwable) : Edit()
    }

    private data class Register(
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

        val dateFilters = FXCollections.observableArrayList(
                FXRegisterEntryFilter("All Dates") { true },
                FXRegisterEntryFilter("Current Month") { it.dateProperty.value.inCurrentMonth() },
                FXRegisterEntryFilter("Current Year") { it.dateProperty.value.inCurrentYear() },
                FXRegisterEntryFilter("Last 3 Months") { it.dateProperty.value.inLastMonths(3) },
                FXRegisterEntryFilter("Last 12 Months") { it.dateProperty.value.inLastMonths(12) }
        )

        dateFiltersProperty = SimpleListProperty(dateFilters)

        selectedDateFilterProperty.value = dateFilters[0]

        val statusFilters = FXCollections.observableArrayList(
                FXRegisterEntryFilter("All Transactions") { true },
                FXRegisterEntryFilter("Unreconciled Transactions") { it.statusProperty.value != TransactionStatus.RECONCILED }
        )

        statusFiltersProperty = SimpleListProperty(statusFilters)

        selectedStatusFilterProperty.value = statusFilters[0]

        val filtersBinding = Bindings.createObjectBinding(
                { selectedDateFilterProperty.value.and(selectedStatusFilterProperty.value) },
                selectedDateFilterProperty,
                selectedStatusFilterProperty
        )

        activeFilters.bind(filtersBinding)

        debitText.bindAsyncValue(accountProperty) { account ->
            TransactionType.Debit(account.accountType).name
        }

        creditText.bindAsyncValue(accountProperty) { account ->
            TransactionType.Credit(account.accountType).name
        }
    }

    fun start(database: ObservableMoneyDatabase, accountIdentity: Long) {

        this.database = database

        val register = SimpleAsyncObjectProperty<Register>()

        register.addListener { _, _, newValue ->
            account.value = newValue.map { it.account }
            transactions.value = newValue.map { it.transactions }
            endingBalance.value = newValue.map { it.endingBalance }
        }

        database.onUpdate.subscribe {
            register.setValueAsync {
                val account = Account.get(accountIdentity, database)
                        ?: throw PersistableNotFoundException(Account::class, accountIdentity)

                var transactions = account.getAccountEntries(database)
                var endingBalance = account.getBalance(database)

                if (account.accountType?.group == AccountTypeGroup.LIABILITIES) {
                    transactions = transactions.map { it.negateBalance() }
                    endingBalance = endingBalance.negate()
                }

                Register(
                        account = account,
                        transactions = transactions.map { FXRegisterEntry(it) },
                        endingBalance = endingBalance
                )
            }
        }.also { disposables.add(it) }
    }

    fun prepareEditEntry(entry: FXRegisterEntry, block: (Edit) -> Unit) {
        when (val t = entry.type) {
            is FXRegisterEntry.Type.Transaction -> prepareEditTransaction(t.transactionId, block)
            is FXRegisterEntry.Type.Transfer -> block.invoke(Edit.Transfer(t.transferId))
        }
    }

    private fun prepareEditTransaction(transactionId: Long, block: (Edit) -> Unit) {
        Single.fromCallable { Transaction.get(transactionId, database) ?: throw PersistableNotFoundException(Transaction::class, transactionId) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe(
                        { block.invoke(Edit.Transaction(it)) },
                        { block.invoke(Edit.Error(it)) }
                )
    }

    fun deleteEntry(entry: FXRegisterEntry, completionBlock: (Throwable?) -> Unit) {
        when (val t = entry.type) {
            is FXRegisterEntry.Type.Transaction -> deleteTransaction(t.transactionId, completionBlock)
            is FXRegisterEntry.Type.Transfer -> deleteTransfer(t.transferId, completionBlock)
        }
    }

    private fun deleteTransaction(transactionId: Long, completionBlock: (Throwable?) -> Unit) {
        Single.fromCallable { deleteTransaction(database, transactionId) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe { _, error -> completionBlock.invoke(error) }
    }

    private fun deleteTransfer(transferId: Long, completionBlock: (Throwable?) -> Unit) {
        Single.fromCallable { deleteTransfer(database, transferId) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe { _, error -> completionBlock.invoke(error) }
    }

    fun updateEntryStatus(entry: FXRegisterEntry, status: TransactionStatus, completionBlock: (Throwable?) -> Unit) {
        Single.fromCallable { entry.updateStatus(status, database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { operationInProgress.value = true }
                .doFinally { operationInProgress.value = false }
                .subscribe { _, error -> completionBlock.invoke(error) }
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

private fun deleteTransaction(executor: QueryExecutor, transactionId: Long) {
    executor.transaction { tx ->

        DeleteQueryBuilder(TransferTable.name)
                .where(TransferTable.transactionColumn.eq(transactionId))
                .build()
                .let { tx.executeUpdate(it) }

        DeleteQueryBuilder(EntryTable.name)
                .where(EntryTable.transactionColumn.eq(transactionId))
                .build()
                .let { tx.executeUpdate(it) }

        DeleteQueryBuilder(TransactionTable.name)
                .where(TransactionTable.identityColumn.eq(transactionId))
                .build()
                .let { tx.executeUpdate(it) }
    }
}

private fun deleteTransfer(executor: QueryExecutor, transferId: Long) {

    DeleteQueryBuilder(TransferTable.name)
            .where(TransferTable.identityColumn.eq(transferId))
            .build()
            .let { executor.executeUpdate(it) }
}
