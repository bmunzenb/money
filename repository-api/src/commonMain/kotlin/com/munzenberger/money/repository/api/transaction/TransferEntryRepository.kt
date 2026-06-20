package com.munzenberger.money.repository.api.transaction

import com.munzenberger.money.repository.api.account.AccountId
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface TransferEntryRepository {
    suspend fun transferEntriesByTransactionId(transactionId: TransactionId): Flow<List<TransferEntry>>

    suspend fun transferEntriesByAccountId(accountId: AccountId): Flow<List<TransferEntry>>

    suspend fun add(transferEntry: TransferEntry)

    suspend fun update(transferEntry: TransferEntry)

    suspend fun removeById(transferEntryId: TransferEntryId)
}

@OptIn(ExperimentalUuidApi::class)
suspend fun TransferEntryRepository.remove(transferEntry: TransferEntry) {
    removeById(transferEntry.id)
}
