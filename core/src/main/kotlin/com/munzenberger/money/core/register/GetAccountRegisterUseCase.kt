package com.munzenberger.money.core.register

import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.register.RegisterEntry
import kotlinx.coroutines.flow.Flow

/**
 * Returns a flow of the register entries of the repository's account with [AccountId], each with the account's balance
 * after it, wrapped in a [Result] (see [resultFlow]). It emits again whenever the entries change.
 *
 * If there's no such account, such as after it's removed, it emits an empty list.
 */
class GetAccountRegisterUseCase {
    operator fun invoke(repository: MoneyRepository, accountId: AccountId): Flow<Result<List<RegisterEntry>>> =
        repository.resultFlow { it.registerEntriesForAccount(accountId) }
}
