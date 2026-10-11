package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId

/**
 * Validates an [AccountInput] and, if it's valid, replaces the existing account with [AccountId] in the
 * repository with it. A new financial institution is added in the same transaction as the account.
 */
class UpdateAccountUseCase {
    suspend operator fun invoke(
        repository: MoneyRepository,
        accountId: AccountId,
        input: AccountInput,
    ): SaveAccountResult =
        repository.saveAccount(input, accountId) { update(it) }
}
