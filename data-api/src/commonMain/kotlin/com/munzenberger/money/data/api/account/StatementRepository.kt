package com.munzenberger.money.data.api.account

import kotlinx.coroutines.flow.Flow

interface StatementRepository {
    suspend fun statementsByAccountId(accountId: AccountId): Flow<List<Statement>>
}
