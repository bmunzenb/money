package com.munzenberger.money.repository.api.bank

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface BankRepository {
    fun allBanks(): Flow<List<Bank>>

    suspend fun add(bank: Bank)

    suspend fun update(bank: Bank)

    suspend fun remove(bank: Bank) {
        removeById(bank.id)
    }

    suspend fun removeById(bankId: BankId)
}
