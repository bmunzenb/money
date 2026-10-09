package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.account.AccountId

/**
 * Validates an [AccountInput] and, if it's valid, replaces the existing account with [AccountId] in the
 * open repository with it. A new financial institution is added in the same transaction as the account.
 */
class UpdateAccountUseCase(
    private val repositoryController: MoneyRepositoryController,
) {
    suspend operator fun invoke(accountId: AccountId, input: AccountInput): SaveAccountResult =
        repositoryController.saveAccount(input, accountId) { update(it) }
}
