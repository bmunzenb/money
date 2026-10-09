package com.munzenberger.money.data.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.payee.PayeeId
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionRepository
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransactionWriter
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.requireOneRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlTransactionRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : TransactionRepository, TransactionWriter {

    override suspend fun transactionsByAccountId(accountId: AccountId): Flow<List<Transaction>> =
        database.transactionQueries
            .selectByAccountId(accountId.value.toString()) { id, accountIdValue, payeeId, date, number, memo, statusId, statusValue ->
                Transaction(
                    id = TransactionId(Uuid.parse(id)),
                    accountId = AccountId(Uuid.parse(accountIdValue)),
                    payeeId = payeeId?.let { PayeeId(Uuid.parse(it)) },
                    date = LocalDate.fromEpochDays(date),
                    number = number,
                    memo = memo,
                    status = TransactionStatus(
                        id = TransactionStatusId(statusId),
                        value = TransactionStatusConstant.valueOf(statusValue),
                    ),
                )
            }
            .asFlow()
            .mapToList(context)

    override fun add(transaction: Transaction) {
        database.transactionQueries.insert(
            id = transaction.id.value.toString(),
            account_id = transaction.accountId.value.toString(),
            payee_id = transaction.payeeId?.value?.toString(),
            date = transaction.date.toEpochDays(),
            number = transaction.number,
            memo = transaction.memo,
            status_id = transaction.status.id.value,
        )
    }

    override fun update(transaction: Transaction) {
        database.transactionQueries.update(
            account_id = transaction.accountId.value.toString(),
            payee_id = transaction.payeeId?.value?.toString(),
            date = transaction.date.toEpochDays(),
            number = transaction.number,
            memo = transaction.memo,
            status_id = transaction.status.id.value,
            id = transaction.id.value.toString(),
        ).requireOneRow { "No transaction with id ${transaction.id.value}" }
    }

    override fun removeById(transactionId: TransactionId) {
        database.transactionQueries.deleteById(transactionId.value.toString())
    }
}
