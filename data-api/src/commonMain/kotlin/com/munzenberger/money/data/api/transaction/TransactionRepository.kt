package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun transactionsByAccountId(accountId: AccountId): Flow<List<Transaction>>

    suspend fun add(transaction: Transaction)

    suspend fun update(transaction: Transaction)

    suspend fun removeById(transactionId: TransactionId)
}

suspend fun TransactionRepository.remove(transaction: Transaction) {
    removeById(transaction.id)
}
