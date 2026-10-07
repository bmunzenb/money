package com.munzenberger.money.data.api.account

import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val accounts: Flow<List<Account>>
}
