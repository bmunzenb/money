package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.Money
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val accounts: Flow<List<Account>>

    fun accountBalance(accountId: AccountId): Flow<Money>
}
