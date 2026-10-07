package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun transactionsByAccountId(accountId: AccountId): Flow<List<Transaction>>
}
