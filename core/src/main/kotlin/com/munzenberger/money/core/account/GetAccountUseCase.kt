package com.munzenberger.money.core.account

import com.munzenberger.money.core.requireFound
import com.munzenberger.money.core.asResult
import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

/**
 * Returns a flow of the repository's account with [AccountId], wrapped in a [Result] (see [asResult]). It emits
 * again whenever the account changes.
 *
 * If there's no such account, such as after it's removed, it emits a failure with an [EntityNotFoundException] (see
 * [requireFound]).
 */
class GetAccountUseCase {
    operator fun invoke(repository: MoneyRepository, accountId: AccountId): Flow<Result<Account>> =
        repository.accountById(accountId)
            .asResult()
            .requireFound { "No account with id ${accountId.value}" }
}
