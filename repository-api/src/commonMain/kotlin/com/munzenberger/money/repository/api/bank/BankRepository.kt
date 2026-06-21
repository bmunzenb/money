package com.munzenberger.money.repository.api.bank

import kotlinx.coroutines.flow.Flow

interface BankRepository {
    val banks: Flow<List<Bank>>

    suspend fun add(bank: Bank)

    suspend fun update(bank: Bank)

    suspend fun removeById(bankId: BankId)
}

suspend fun BankRepository.remove(bank: Bank) {
    removeById(bank.id)
}
