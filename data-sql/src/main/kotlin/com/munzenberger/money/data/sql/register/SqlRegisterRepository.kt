package com.munzenberger.money.data.sql.register

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.register.RegisterRepository
import com.munzenberger.money.data.api.transaction.CategoryEntryId
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntryId
import com.munzenberger.money.data.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

/**
 * Builds an account's register from its own transactions and from the transfer entries of other
 * transactions that move money into or out of it.
 *
 * Entries are ordered by date, then by when their transaction was created (a transfer uses its
 * parent transaction's date and created time). Each entry's balance is the account's initial balance
 * plus the amounts of every entry up to and including it.
 */
class SqlRegisterRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : RegisterRepository {

    override fun registerEntriesForAccount(accountId: AccountId): Flow<List<RegisterEntry>> {
        val id = accountId.value
        return combine(
            database.registerQueries.selectInitialBalance(id).asFlow().mapToOneOrNull(context),
            transactionRows(id),
            categoryEntryRows(id),
            transferEntryRows(id),
            transferRows(id),
        ) { initialBalance, transactions, categoryEntries, transferEntries, transfers ->
            if (initialBalance == null) {
                emptyList()
            } else {
                buildRegister(
                    initialBalance = Money(initialBalance),
                    transactions = transactions,
                    entries = categoryEntries + transferEntries,
                    transfers = transfers,
                )
            }
        }
    }

    private fun transactionRows(accountId: Uuid): Flow<List<TransactionRow>> =
        database.registerQueries
            .selectTransactions(accountId) {
                transactionId,
                payeeId,
                payeeName,
                date,
                created,
                number,
                memo,
                statusId,
                statusValue,
                ->
                TransactionRow(
                    transactionId = transactionId,
                    payeeId = payeeId?.let { PayeeId(it) },
                    payeeName = payeeName,
                    date = LocalDate.fromEpochDays(date),
                    created = created,
                    number = number,
                    memo = memo,
                    status = transactionStatus(statusId, statusValue),
                )
            }
            .asFlow()
            .mapToList(context)

    private fun categoryEntryRows(accountId: Uuid): Flow<List<EntryRow>> =
        database.registerQueries
            .selectCategoryEntries(accountId) {
                categoryEntryId,
                transactionId,
                categoryId,
                categoryName,
                parentCategoryId,
                parentCategoryName,
                amount,
                orderInTransaction,
                ->
                EntryRow(
                    transactionId = transactionId,
                    orderInTransaction = orderInTransaction,
                    entry = RegisterEntry.Transaction.Entry.Category(
                        categoryEntryId = CategoryEntryId(categoryEntryId),
                        categoryId = CategoryId(categoryId),
                        categoryName = categoryName,
                        parentCategoryId = parentCategoryId?.let { CategoryId(it) },
                        parentCategoryName = parentCategoryName,
                        amount = Money(amount),
                    ),
                )
            }
            .asFlow()
            .mapToList(context)

    private fun transferEntryRows(accountId: Uuid): Flow<List<EntryRow>> =
        database.registerQueries
            .selectTransferEntries(accountId) {
                transferEntryId,
                transactionId,
                transferAccountId,
                accountName,
                amount,
                orderInTransaction,
                ->
                EntryRow(
                    transactionId = transactionId,
                    orderInTransaction = orderInTransaction,
                    entry = RegisterEntry.Transaction.Entry.Transfer(
                        transferEntryId = TransferEntryId(transferEntryId),
                        accountId = AccountId(transferAccountId),
                        accountName = accountName,
                        amount = Money(amount),
                    ),
                )
            }
            .asFlow()
            .mapToList(context)

    private fun transferRows(accountId: Uuid): Flow<List<TransferRow>> =
        database.registerQueries
            .selectTransfers(accountId) {
                transferEntryId,
                payeeId,
                payeeName,
                date,
                created,
                amount,
                number,
                memo,
                statusId,
                statusValue,
                accountIdValue,
                accountName,
                ->
                TransferRow(
                    transferEntryId = TransferEntryId(transferEntryId),
                    payeeId = payeeId?.let { PayeeId(it) },
                    payeeName = payeeName,
                    date = LocalDate.fromEpochDays(date),
                    created = created,
                    // The transfer entry's amount is from its parent transaction's account's view.
                    amount = -Money(amount),
                    number = number,
                    memo = memo,
                    status = transactionStatus(statusId, statusValue),
                    accountId = AccountId(accountIdValue),
                    accountName = accountName,
                )
            }
            .asFlow()
            .mapToList(context)

    private fun buildRegister(
        initialBalance: Money,
        transactions: List<TransactionRow>,
        entries: List<EntryRow>,
        transfers: List<TransferRow>,
    ): List<RegisterEntry> {
        val entriesByTransactionId = entries
            .groupBy { it.transactionId }
            .mapValues { (_, rows) -> rows.sortedBy { it.orderInTransaction }.map { it.entry } }

        val rows = transactions.map { transaction ->
            transaction.toRegisterRow(entriesByTransactionId[transaction.transactionId].orEmpty())
        } + transfers.map { it.toRegisterRow() }

        var balance = initialBalance
        return rows
            .sortedWith(compareBy<RegisterRow>({ it.date }, { it.created }))
            .map { row ->
                balance += row.amount
                row.toEntry(balance)
            }
    }

    private fun transactionStatus(id: Long, value: String) = TransactionStatus(
        id = TransactionStatusId(id),
        value = TransactionStatusConstant.valueOf(value),
    )

    /** A register entry before its balance is known. */
    private class RegisterRow(
        val date: LocalDate,
        val created: Long,
        val amount: Money,
        val toEntry: (balance: Money) -> RegisterEntry,
    )

    private data class TransactionRow(
        val transactionId: Uuid,
        val payeeId: PayeeId?,
        val payeeName: String?,
        val date: LocalDate,
        val created: Long,
        val number: String?,
        val memo: String?,
        val status: TransactionStatus,
    ) {
        fun toRegisterRow(entries: List<RegisterEntry.Transaction.Entry>): RegisterRow {
            val amount = entries.fold(Money(0)) { sum, entry -> sum + entry.amount }
            return RegisterRow(
                date = date,
                created = created,
                amount = amount,
                toEntry = { balance ->
                    RegisterEntry.Transaction(
                        transactionId = TransactionId(transactionId),
                        payeeId = payeeId,
                        payeeName = payeeName,
                        date = date,
                        amount = amount,
                        balance = balance,
                        number = number,
                        memo = memo,
                        status = status,
                        entries = entries,
                    )
                },
            )
        }
    }

    private data class EntryRow(
        val transactionId: Uuid,
        val orderInTransaction: Long,
        val entry: RegisterEntry.Transaction.Entry,
    )

    private data class TransferRow(
        val transferEntryId: TransferEntryId,
        val payeeId: PayeeId?,
        val payeeName: String?,
        val date: LocalDate,
        val created: Long,
        val amount: Money,
        val number: String?,
        val memo: String?,
        val status: TransactionStatus,
        val accountId: AccountId,
        val accountName: String,
    ) {
        fun toRegisterRow() = RegisterRow(
            date = date,
            created = created,
            amount = amount,
            toEntry = { balance ->
                RegisterEntry.Transfer(
                    transferEntryId = transferEntryId,
                    payeeId = payeeId,
                    payeeName = payeeName,
                    date = date,
                    amount = amount,
                    balance = balance,
                    number = number,
                    memo = memo,
                    status = status,
                    accountId = accountId,
                    accountName = accountName,
                )
            },
        )
    }
}
