package com.munzenberger.money.data.api.account

import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    val accounts: Flow<List<Account>>

    suspend fun add(account: Account)

    suspend fun update(account: Account)

    suspend fun removeById(accountId: AccountId)
}

suspend fun AccountRepository.remove(account: Account) {
    removeById(account.id)
}
