package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.model.FXAccountEntryFilter
import com.munzenberger.money.app.model.FXTransactionAccountEntry
import com.munzenberger.money.app.model.FXTransferAccountEntry
import com.munzenberger.money.app.model.inCurrentMonth
import com.munzenberger.money.app.model.inCurrentYear
import com.munzenberger.money.app.model.inLastMonths
import com.munzenberger.money.app.observable.CompositeSubscription
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.bindAsyncValue
import com.munzenberger.money.app.property.map
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountEntry
import com.munzenberger.money.core.AccountIdentity
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.PersistableNotFoundException
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransactionIdentity
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.TransferEntryIdentity
import com.munzenberger.money.core.getAccountEntries
import com.munzenberger.money.core.getBalance
import com.munzenberger.money.core.model.AccountTypeGroup
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
import javafx.concurrent.Task
import java.util.function.Predicate

class AccountRegisterViewModel : AccountEntriesViewModel, AutoCloseable {

    sealed class Edit {
        data class Transaction(val transaction: com.munzenberger.money.core.Transaction) : Edit()
        data class Transfer(val transferId: TransferEntryIdentity) : Edit()
        data class Error(val error: Throwable) : Edit()
    }

    private data class Register(
            val account: Account,
            val transactions: List<FXAccountEntry>,
            val endingBalance: Money
    )

    private val subscriptions = CompositeSubscription()

    private val account = SimpleAsyncObjectProperty<Account>()
    private val transactions = SimpleAsyncObjectProperty<List<FXAccountEntry>>()
    private val endingBalance = SimpleAsyncObjectProperty<Money>()
    private val amountText = SimpleStringProperty()
    private val activeFilters = SimpleObjectProperty<Predicate<FXAccountEntry>>()
    private val isOperationInProgress = SimpleBooleanProperty(false)
    private val register = SimpleAsyncObjectProperty<Register>()

    val accountProperty: ReadOnlyAsyncObjectProperty<Account> = account
    override val transactionsProperty: ReadOnlyAsyncObjectProperty<List<FXAccountEntry>> = transactions
    val endingBalanceProperty: ReadOnlyAsyncObjectProperty<Money> = endingBalance
    override val amountTextProperty: ReadOnlyStringProperty = amountText
    val dateFiltersProperty: ReadOnlyListProperty<FXAccountEntryFilter>
    val statusFiltersProperty: ReadOnlyListProperty<FXAccountEntryFilter>
    val activeFiltersProperty: ReadOnlyObjectProperty<Predicate<FXAccountEntry>> = activeFilters
    val isOperationInProgressProperty: ReadOnlyBooleanProperty = isOperationInProgress

    val selectedDateFilterProperty = SimpleObjectProperty<FXAccountEntryFilter>()
    val selectedStatusFilterProperty = SimpleObjectProperty<FXAccountEntryFilter>()

    lateinit var database: MoneyDatabase

    init {

        val dateFilters = FXCollections.observableArrayList(
                FXAccountEntryFilter("All Dates") { true },
                FXAccountEntryFilter("Current Month") { it.dateProperty.value.inCurrentMonth() },
                FXAccountEntryFilter("Current Year") { it.dateProperty.value.inCurrentYear() },
                FXAccountEntryFilter("Last 3 Months") { it.dateProperty.value.inLastMonths(3) },
                FXAccountEntryFilter("Last 12 Months") { it.dateProperty.value.inLastMonths(12) }
        )

        dateFiltersProperty = SimpleListProperty(dateFilters)

        selectedDateFilterProperty.value = dateFilters[0]

        val statusFilters = FXCollections.observableArrayList(
                FXAccountEntryFilter("All Transactions") { true },
                FXAccountEntryFilter("Unreconciled Transactions") { it.statusProperty.value != TransactionStatus.RECONCILED }
        )

        statusFiltersProperty = SimpleListProperty(statusFilters)

        selectedStatusFilterProperty.value = statusFilters[0]

        val filtersBinding = Bindings.createObjectBinding(
                { selectedDateFilterProperty.value.and(selectedStatusFilterProperty.value) },
                selectedDateFilterProperty,
                selectedStatusFilterProperty
        )

        activeFilters.bind(filtersBinding)

        amountText.bindAsyncValue(accountProperty) { account ->
            val debitName = TransactionType.Debit(account.accountType).name
            val creditName = TransactionType.Credit(account.accountType).name
            "$debitName / $creditName"
        }

        register.addListener { _, _, newValue ->
            account.value = newValue.map { it.account }
            transactions.value = newValue.map { it.transactions }
            endingBalance.value = newValue.map { it.endingBalance }
        }
    }

    fun start(database: ObservableMoneyDatabase, accountIdentity: AccountIdentity) {

        this.database = database

        database.subscribe {
            register.setValueAsync {
                val account = Account.get(accountIdentity, database)
                        ?: throw PersistableNotFoundException(Account::class, accountIdentity)

                var transactions = account.getAccountEntries(database)
                var endingBalance = account.getBalance(database)

                if (account.accountType?.group == AccountTypeGroup.LIABILITIES) {
                    transactions = transactions.map { it.negate() }
                    endingBalance = endingBalance.negate()
                }

                Register(
                        account = account,
                        transactions = transactions.map { FXAccountEntry.of(it) },
                        endingBalance = endingBalance
                )
            }
        }.also { subscriptions.add(it) }
    }

    fun prepareEditEntry(entry: FXAccountEntry, block: (Edit) -> Unit) {
        when (entry) {
            is FXTransactionAccountEntry -> prepareEditTransaction(entry.transactionId, block)
            is FXTransferAccountEntry -> block.invoke(Edit.Transfer(entry.transferId))
        }
    }

    private fun prepareEditTransaction(transactionId: TransactionIdentity, block: (Edit) -> Unit) {

        val task = object : Task<Transaction>() {

            override fun call(): Transaction {
                return Transaction.get(transactionId, database)
                        ?: throw PersistableNotFoundException(Transaction::class, transactionId)
            }

            override fun succeeded() {
                block.invoke(Edit.Transaction(value))
            }

            override fun failed() {
                block.invoke(Edit.Error(exception))
            }
        }

        isOperationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    fun deleteEntry(entry: FXAccountEntry, completionBlock: (Throwable?) -> Unit) {

        val task = object : Task<Unit>() {

            override fun call() {
                entry.delete(database)
            }

            override fun succeeded() {
                completionBlock.invoke(null)
            }

            override fun failed() {
                completionBlock.invoke(exception)
            }
        }

        isOperationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    override fun updateEntryStatus(entry: FXAccountEntry, status: TransactionStatus, completionBlock: (Throwable?) -> Unit) {

        val task = object : Task<Unit>() {

            override fun call() {
                entry.updateStatus(status, database)
            }

            override fun succeeded() {
                completionBlock.invoke(null)
            }

            override fun failed() {
                completionBlock.invoke(exception)
            }
        }

        isOperationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }

    override fun close() {
        subscriptions.cancel()
    }
}

private fun AccountEntry.negate(): AccountEntry {
    return when (this) {
        is AccountEntry.Transaction -> copy(balance = balance.negate(), amount = amount.negate())
        is AccountEntry.Transfer -> copy(balance = balance.negate(), amount = amount.negate())
    }
}
