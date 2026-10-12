package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId

/**
 * Validates an [AccountInput] and, if it's valid, adds it to the repository as a new, open account. A new
 * financial institution is added in the same transaction as the account.
 */
class CreateAccountUseCase {
    suspend operator fun invoke(repository: MoneyRepository, input: AccountInput): SaveAccountResult =
        repository.saveAccount(input, AccountId(), isClosed = false) { add(it) }
}
