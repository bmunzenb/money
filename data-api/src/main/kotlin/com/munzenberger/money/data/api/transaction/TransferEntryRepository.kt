package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

interface TransferEntryRepository {
    fun transferEntriesByTransactionId(transactionId: TransactionId): Flow<List<TransferEntry>>

    fun transferEntriesByAccountId(accountId: AccountId): Flow<List<TransferEntry>>
}
