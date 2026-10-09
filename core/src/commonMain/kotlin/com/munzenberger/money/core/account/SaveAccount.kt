package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException

sealed interface SaveAccountResult {
    data class Success(val account: Account) : SaveAccountResult

    /** The input failed validation, so nothing was written. */
    data class Invalid(val errors: Set<AccountInputError>) : SaveAccountResult

    /** The input was valid, but the account couldn't be written, or no repository is open. */
    data class Failure(val cause: Throwable) : SaveAccountResult
}

/**
 * Validates [input] and, if it's valid, writes it to the open repository as the account with [accountId].
 * [write] writes the account; a new financial institution named in [input] is added before it, in the
 * same transaction.
 */
internal suspend fun MoneyRepositoryController.saveAccount(
    input: AccountInput,
    accountId: AccountId,
    write: MoneyWriter.(Account) -> Unit,
): SaveAccountResult {
    val (account, newBank) = when (val validated = input.validate(accountId)) {
        is ValidatedAccountInput.Invalid -> return SaveAccountResult.Invalid(validated.errors)
        is ValidatedAccountInput.Valid -> validated
    }

    val repository = moneyRepository.value
        ?: return SaveAccountResult.Failure(IllegalStateException("No money repository is open"))

    return try {
        repository.transaction {
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
