package com.munzenberger.money.data.api.account

import kotlinx.coroutines.flow.Flow

interface StatementRepository {
    fun statementsByAccountId(accountId: AccountId): Flow<List<Statement>>
}
