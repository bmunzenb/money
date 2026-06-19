package com.munzenberger.money.repository.api.account

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
interface AccountRepository {
    val accounts: Flow<List<Account>>

    suspend fun add(account: Account)

    suspend fun update(account: Account)

    suspend fun removeById(accountId: AccountId)
}

@OptIn(ExperimentalUuidApi::class)
suspend fun AccountRepository.remove(account: Account) {
    removeById(account.id)
}
