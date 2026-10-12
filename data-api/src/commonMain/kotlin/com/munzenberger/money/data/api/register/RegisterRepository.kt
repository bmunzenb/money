package com.munzenberger.money.data.api.register

import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    fun registerEntriesForAccount(accountId: AccountId): Flow<List<RegisterEntry>>
}
