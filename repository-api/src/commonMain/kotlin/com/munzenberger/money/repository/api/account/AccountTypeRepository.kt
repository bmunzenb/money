package com.munzenberger.money.repository.api.account

import kotlinx.coroutines.flow.Flow

interface AccountTypeRepository {
    val accountTypes: Flow<List<AccountType>>
}
