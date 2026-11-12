package com.munzenberger.money.data.api.transaction

import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun transactionById(transactionId: TransactionId): Flow<Transaction?>
}
