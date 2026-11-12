package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun transactionsByAccountId(accountId: AccountId): Flow<List<Transaction>>

    fun transactionById(transactionId: TransactionId): Flow<Transaction?>
}
