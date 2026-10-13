package com.munzenberger.money.data.api.bank

import kotlinx.coroutines.flow.Flow

interface BankRepository {
    val banks: Flow<List<Bank>>
}
