package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.Money
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val accounts: Flow<List<Account>>

    /** The balance of each account, keyed by its id. */
    val accountBalances: Flow<Map<AccountId, Money>>

    /** The account with [accountId], or null if there's none, such as after it's removed. */
    fun accountById(accountId: AccountId): Flow<Account?>
}
