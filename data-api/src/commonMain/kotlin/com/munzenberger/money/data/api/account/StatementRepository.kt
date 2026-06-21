package com.munzenberger.money.data.api.account

import kotlinx.coroutines.flow.Flow

interface StatementRepository {
    suspend fun statementsByAccountId(accountId: AccountId): Flow<List<Statement>>

    suspend fun add(statement: Statement)

    suspend fun update(statement: Statement)

    suspend fun removeById(statementId: StatementId)
}

suspend fun StatementRepository.remove(statement: Statement) {
    removeById(statement.id)
}
