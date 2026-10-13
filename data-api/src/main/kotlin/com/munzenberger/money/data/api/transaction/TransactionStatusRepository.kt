package com.munzenberger.money.data.api.transaction

import kotlinx.coroutines.flow.Flow

interface TransactionStatusRepository {
    val transactionStatuses: Flow<List<TransactionStatus>>
}
