package com.munzenberger.money.repository.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.repository.api.Money
import com.munzenberger.money.repository.api.account.AccountId
import com.munzenberger.money.repository.api.transaction.TransactionId
import com.munzenberger.money.repository.api.transaction.TransactionStatus
import com.munzenberger.money.repository.api.transaction.TransactionStatusConstant
import com.munzenberger.money.repository.api.transaction.TransactionStatusId
import com.munzenberger.money.repository.api.transaction.TransferEntry
import com.munzenberger.money.repository.api.transaction.TransferEntryId
import com.munzenberger.money.repository.api.transaction.TransferEntryRepository
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlTransferEntryRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : TransferEntryRepository {

    override suspend fun transferEntriesByTransactionId(transactionId: TransactionId): Flow<List<TransferEntry>> =
        database.transferEntryQueries
            .selectByTransactionId(transactionId.value.toString(), ::mapTransferEntry)
            .asFlow()
            .mapToList(context)

    override suspend fun transferEntriesByAccountId(accountId: AccountId): Flow<List<TransferEntry>> =
        database.transferEntryQueries
            .selectByAccountId(accountId.value.toString(), ::mapTransferEntry)
            .asFlow()
            .mapToList(context)

    override suspend fun add(transferEntry: TransferEntry) {
        withContext(context) {
            database.transferEntryQueries.insert(
                id = transferEntry.id.id.toString(),
                transaction_id = transferEntry.transactionId.value.toString(),
                account_id = transferEntry.accountId.value.toString(),
                amount = transferEntry.amount.value,
                number = transferEntry.number,
                memo = transferEntry.memo,
                status_id = transferEntry.status.id.value,
                order_in_transaction = transferEntry.orderInTransaction.toLong(),
            )
        }
    }

    override suspend fun update(transferEntry: TransferEntry) {
        withContext(context) {
            database.transferEntryQueries.update(
                transaction_id = transferEntry.transactionId.value.toString(),
                account_id = transferEntry.accountId.value.toString(),
                amount = transferEntry.amount.value,
                number = transferEntry.number,
                memo = transferEntry.memo,
                status_id = transferEntry.status.id.value,
                order_in_transaction = transferEntry.orderInTransaction.toLong(),
                id = transferEntry.id.id.toString(),
            )
        }
    }

    override suspend fun removeById(transferEntryId: TransferEntryId) {
        withContext(context) {
            database.transferEntryQueries.deleteById(transferEntryId.id.toString())
        }
    }

    private fun mapTransferEntry(
        id: String,
        transactionId: String,
        accountId: String,
        amount: Long,
        number: String?,
        memo: String?,
        statusId: Long,
        statusValue: String,
        orderInTransaction: Long,
    ) = TransferEntry(
        id = TransferEntryId(Uuid.parse(id)),
        transactionId = TransactionId(Uuid.parse(transactionId)),
        accountId = AccountId(Uuid.parse(accountId)),
        amount = Money(amount),
        number = number,
        memo = memo,
        status = TransactionStatus(
            id = TransactionStatusId(statusId),
            value = TransactionStatusConstant.valueOf(statusValue),
        ),
        orderInTransaction = orderInTransaction.toInt(),
    )
}
