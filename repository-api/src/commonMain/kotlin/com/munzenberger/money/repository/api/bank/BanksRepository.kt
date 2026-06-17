package com.munzenberger.money.repository.api.bank

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface BanksRepository {
    val banks: Flow<ModelState<List<Bank>>>

    suspend fun create(bank: Bank)

    suspend fun update(bank: Bank)

    @OptIn(ExperimentalUuidApi::class)
    suspend fun delete(id: BankId)
}
