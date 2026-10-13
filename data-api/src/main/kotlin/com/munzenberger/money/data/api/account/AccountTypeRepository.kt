package com.munzenberger.money.data.api.account

import kotlinx.coroutines.flow.Flow

interface AccountTypeRepository {
    val accountTypes: Flow<List<AccountType>>
}
