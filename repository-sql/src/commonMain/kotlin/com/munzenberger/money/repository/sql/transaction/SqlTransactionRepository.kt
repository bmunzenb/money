package com.munzenberger.money.repository.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.repository.api.account.AccountId
import com.munzenberger.money.repository.api.payee.PayeeId
import com.munzenberger.money.repository.api.transaction.Transaction
import com.munzenberger.money.repository.api.transaction.TransactionId
import com.munzenberger.money.repository.api.transaction.TransactionRepository
import com.munzenberger.money.repository.api.transaction.TransactionStatus
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SqlTransactionRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : TransactionRepository {

    override suspend fun transactionsByAccountId(accountId: AccountId): Flow<List<Transaction>> =
        database.transactionQueries
            .selectByAccountId(accountId.value.toString()) { id, accountIdValue, payeeId, date, number, memo, status ->
                Transaction(
                    id = TransactionId(Uuid.parse(id)),
                    accountId = AccountId(Uuid.parse(accountIdValue)),
                    payeeId = payeeId?.let { PayeeId(Uuid.parse(it)) },
                    date = LocalDate.fromEpochDays(date),
                    number = number,
                    memo = memo,
                    status = TransactionStatus.valueOf(status),
                )
            }
            .asFlow()
            .mapToList(context)

    override suspend fun add(transaction: Transaction) {
        withContext(context) {
            database.transactionQueries.insert(
                id = transaction.id.value.toString(),
                account_id = transaction.accountId.value.toString(),
                payee_id = transaction.payeeId?.value?.toString(),
                date = transaction.date.toEpochDays(),
                number = transaction.number,
                memo = transaction.memo,
                status = transaction.status.name,
            )
        }
    }

    override suspend fun update(transaction: Transaction) {
        withContext(context) {
            database.transactionQueries.update(
                account_id = transaction.accountId.value.toString(),
                payee_id = transaction.payeeId?.value?.toString(),
                date = transaction.date.toEpochDays(),
                number = transaction.number,
                memo = transaction.memo,
                status = transaction.status.name,
                id = transaction.id.value.toString(),
            )
        }
    }

    override suspend fun removeById(transactionId: TransactionId) {
        withContext(context) {
            database.transactionQueries.deleteById(transactionId.value.toString())
        }
    }
}
