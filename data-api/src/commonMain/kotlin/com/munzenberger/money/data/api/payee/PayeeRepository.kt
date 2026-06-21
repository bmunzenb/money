package com.munzenberger.money.data.api.payee

import kotlinx.coroutines.flow.Flow

interface PayeeRepository {
    val payees: Flow<List<Payee>>

    suspend fun add(payee: Payee)

    suspend fun update(payee: Payee)

    suspend fun removeById(payeeId: PayeeId)
}

suspend fun PayeeRepository.remove(payee: Payee) {
    removeById(payee.id)
}
