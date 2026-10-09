package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.account.Account

sealed interface SaveAccountResult {
    data class Success(val account: Account) : SaveAccountResult

    /** The input failed validation, so nothing was written. */
    data class Invalid(val errors: Set<AccountInputError>) : SaveAccountResult

    /** The input was valid, but the account couldn't be written, or no repository is open. */
    data class Failure(val cause: Throwable) : SaveAccountResult
}
