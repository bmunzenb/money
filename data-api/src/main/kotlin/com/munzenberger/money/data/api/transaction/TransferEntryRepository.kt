package com.munzenberger.money.data.api.transaction

import kotlinx.coroutines.flow.Flow

interface TransferEntryRepository {
    fun transferEntriesByTransactionId(transactionId: TransactionId): Flow<List<TransferEntry>>
}
