package com.munzenberger.money.data.sql.transaction

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import com.munzenberger.money.data.api.transaction.TransferEntryId
import com.munzenberger.money.data.api.transaction.TransferEntryRepository
import com.munzenberger.money.data.api.transaction.TransferEntryWriter
import com.munzenberger.money.data.sql.MoneyDatabase
import com.munzenberger.money.data.sql.requireOneRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class SqlTransferEntryRepository(
    private val database: MoneyDatabase,
    private val context: CoroutineContext = Dispatchers.IO,
) : TransferEntryRepository, TransferEntryWriter {

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

    override fun add(transferEntry: TransferEntry) {
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

    override fun update(transferEntry: TransferEntry) {
        database.transferEntryQueries.update(
            transaction_id = transferEntry.transactionId.value.toString(),
            account_id = transferEntry.accountId.value.toString(),
            amount = transferEntry.amount.value,
            number = transferEntry.number,
            memo = transferEntry.memo,
            status_id = transferEntry.status.id.value,
            order_in_transaction = transferEntry.orderInTransaction.toLong(),
            id = transferEntry.id.id.toString(),
        ).requireOneRow { "No transfer entry with id ${transferEntry.id.id}" }
    }

    override fun removeById(transferEntryId: TransferEntryId) {
        database.transferEntryQueries.deleteById(transferEntryId.id.toString())
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
