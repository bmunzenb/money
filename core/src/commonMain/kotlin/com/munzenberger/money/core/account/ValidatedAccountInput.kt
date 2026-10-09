package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.bank.Bank

/** An [AccountInput] that's been validated: either the account to write, or why it can't be written. */
internal sealed interface ValidatedAccountInput {
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
internal fun AccountInput.validate(accountId: AccountId): ValidatedAccountInput {
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
