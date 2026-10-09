package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.account.AccountId

/**
 * Validates an [AccountInput] and, if it's valid, adds it to the open repository as a new account. A new
 * financial institution is added in the same transaction as the account.
 */
class CreateAccountUseCase(
    private val repositoryController: MoneyRepositoryController,
) {
    suspend operator fun invoke(input: AccountInput): SaveAccountResult =
        repositoryController.saveAccount(input, AccountId()) { add(it) }
}
