package com.munzenberger.money.data.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionRepository
import com.munzenberger.money.data.api.transaction.TransactionWriter
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.requireOneRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class SqlTransactionRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
    private val clock: Clock = Clock.System,
) : TransactionRepository, TransactionWriter {

    override fun transactionsByAccountId(accountId: AccountId): Flow<List<Transaction>> =
        database.transactionQueries
            .selectByAccountId(accountId.value, ::transaction)
            .asFlow()
            .mapToList(context)

    override fun transactionById(transactionId: TransactionId): Flow<Transaction?> =
        database.transactionQueries
            .selectById(transactionId.value, ::transaction)
            .asFlow()
            .mapToOneOrNull(context)

    override fun add(transaction: Transaction) {
        database.transactionQueries.insert(
            id = transaction.id.value,
            account_id = transaction.accountId.value,
            payee_id = transaction.payeeId?.value,
            date = transaction.date.toEpochDays(),
            number = transaction.number,
            memo = transaction.memo,
            status_id = transaction.status.id.value,
            created = clock.now().toEpochMilliseconds(),
        )
    }

    override fun update(transaction: Transaction) {
        database.transactionQueries.update(
            account_id = transaction.accountId.value,
            payee_id = transaction.payeeId?.value,
            date = transaction.date.toEpochDays(),
            number = transaction.number,
            memo = transaction.memo,
            status_id = transaction.status.id.value,
            id = transaction.id.value,
        ).requireOneRow { "No transaction with id ${transaction.id.value}" }
    }

    override fun removeById(transactionId: TransactionId) {
        database.transactionQueries.deleteById(transactionId.value)
    }
}

/** Maps a row of `selectByAccountId` or `selectById` to a [Transaction]. */
@Suppress("LongParameterList")
private fun transaction(
    id: Uuid,
    accountId: Uuid,
    payeeId: Uuid?,
    date: Long,
    number: String?,
    memo: String?,
    statusId: Long,
    statusValue: String,
) = Transaction(
    id = TransactionId(id),
    accountId = AccountId(accountId),
    payeeId = payeeId?.let { PayeeId(it) },
    date = LocalDate.fromEpochDays(date),
    number = number,
    memo = memo,
    status = transactionStatus(statusId, statusValue),
)
