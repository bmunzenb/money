package com.munzenberger.money.repository.api.payee

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface PayeeRepository {
    val payees: Flow<List<Payee>>

    suspend fun add(payee: Payee)

    suspend fun update(payee: Payee)

    suspend fun remove(payee: Payee) {
        removeById(payee.id)
    }

    suspend fun removeById(payeeId: PayeeId)
}
