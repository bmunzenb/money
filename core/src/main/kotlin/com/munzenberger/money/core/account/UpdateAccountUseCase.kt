package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId

/**
 * Validates an [AccountInput] and, if it's valid, replaces the existing account with [AccountId] in the
 * repository with it, closed if `isClosed`. A new financial institution is added in the same transaction
 * as the account.
 */
class UpdateAccountUseCase {
    suspend operator fun invoke(
        repository: MoneyRepository,
        accountId: AccountId,
        input: AccountInput,
        isClosed: Boolean,
    ): SaveAccountResult =
        repository.saveAccount(input, accountId, isClosed) { update(it) }
}
