package com.munzenberger.money.core.account

import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException

sealed interface SaveAccountResult {
    data class Success(val account: Account) : SaveAccountResult

    /** The input failed validation, so nothing was written. */
    data class Invalid(val errors: Set<AccountInputError>) : SaveAccountResult

    /** The input was valid, but the account couldn't be written. */
    data class Failure(val cause: Throwable) : SaveAccountResult
}

/**
 * Validates [input] and, if it's valid, writes it to this repository as the account with [accountId].
 * [write] writes the account; a new financial institution named in [input] is added before it, in the
 * same transaction.
 */
internal suspend fun MoneyRepository.saveAccount(
    input: AccountInput,
    accountId: AccountId,
    write: MoneyWriter.(Account) -> Unit,
): SaveAccountResult {
    val (account, newBank) = when (val validated = input.validate(accountId)) {
        is ValidatedAccountInput.Invalid -> return SaveAccountResult.Invalid(validated.errors)
        is ValidatedAccountInput.Valid -> validated
    }

    return try {
        transaction {
            newBank?.let { add(it) }
            write(account)
        }
        SaveAccountResult.Success(account)
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.log(Level.WARNING, "Failed to save the account", e)
        SaveAccountResult.Failure(e)
    }
}
