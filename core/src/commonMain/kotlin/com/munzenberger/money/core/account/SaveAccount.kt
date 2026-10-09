package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.bank.Bank
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

/** An [AccountInput] that's been validated: either the account to write, or why it can't be written. */
private sealed interface ValidatedAccountInput {
    /**
     * The [account] to write, and [newBank], the financial institution to add along with it, or null if
     * the account has an existing one or none.
     */
    data class Valid(val account: Account, val newBank: Bank?) : ValidatedAccountInput

    data class Invalid(val errors: Set<AccountInputError>) : ValidatedAccountInput
}

/**
 * Validates this input and, if it's valid, turns it into the account with [accountId]: values are
 * trimmed, and blank optional values are left out. A financial institution named in [AccountInput.bankName]
 * that isn't an existing bank becomes a new one for the account.
 */
private fun AccountInput.validate(accountId: AccountId): ValidatedAccountInput {
    val initialBalance = parseInitialBalance(initialBalance)
    val errors = buildSet {
        if (name.isBlank()) add(AccountInputError.BlankName)
        if (accountType == null) add(AccountInputError.MissingAccountType)
        if (initialBalance == null) add(AccountInputError.InvalidInitialBalance)
    }
    if (errors.isNotEmpty() || accountType == null || initialBalance == null) {
        return ValidatedAccountInput.Invalid(errors)
    }

    val newBank = bankName.trim()
        .takeIf { bank == null && it.isNotEmpty() }
        ?.let { Bank(name = it) }

    val account = Account(
        id = accountId,
        name = name.trim(),
        number = number.trim().ifEmpty { null },
        accountType = accountType,
        bankId = (bank ?: newBank)?.id,
        initialBalance = initialBalance,
        memo = memo.trim().ifEmpty { null },
    )

    return ValidatedAccountInput.Valid(account, newBank)
}
