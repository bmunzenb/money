package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

interface TransferEntryRepository {
    suspend fun transferEntriesByTransactionId(transactionId: TransactionId): Flow<List<TransferEntry>>

    suspend fun transferEntriesByAccountId(accountId: AccountId): Flow<List<TransferEntry>>

    suspend fun add(transferEntry: TransferEntry)

    suspend fun update(transferEntry: TransferEntry)

    suspend fun removeById(transferEntryId: TransferEntryId)
}

suspend fun TransferEntryRepository.remove(transferEntry: TransferEntry) {
    removeById(transferEntry.id)
}
