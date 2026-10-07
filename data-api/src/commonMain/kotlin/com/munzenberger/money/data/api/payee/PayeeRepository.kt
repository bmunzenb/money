package com.munzenberger.money.data.api.payee

import kotlinx.coroutines.flow.Flow

interface PayeeRepository {
    val payees: Flow<List<Payee>>
}
