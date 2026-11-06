package com.munzenberger.money.core.account

import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns a flow of the repository's account with [AccountId], wrapped in a [Result] (see [resultFlow]). It emits
 * again whenever the account changes.
 *
 * If there's no such account, such as after it's removed, it emits a failure with an [EntityNotFoundException]. That
 * isn't logged as a failed read, and it doesn't end the flow.
 */
class GetAccountUseCase {
    operator fun invoke(repository: MoneyRepository, accountId: AccountId): Flow<Result<Account>> =
        repository.resultFlow { it.accountById(accountId) }
            .map { result ->
                result.mapCatching { it ?: throw EntityNotFoundException("No account with id ${accountId.value}") }
            }
}
