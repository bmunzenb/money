package com.munzenberger.money.repository.api.transaction

import kotlinx.coroutines.flow.Flow

interface TransactionStatusRepository {
    val transactionStatuses: Flow<List<TransactionStatus>>
}
