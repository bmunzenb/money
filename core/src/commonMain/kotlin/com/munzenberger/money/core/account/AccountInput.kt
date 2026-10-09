package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank

/** The values entered for an account, as typed into the form. */
data class AccountInput(
    val name: String,
    val accountType: AccountType?,
    /** The financial institution's name: an existing bank's name, a new one, or blank for none. */
    val bankName: String,
    /** The existing bank to use, or null to add a bank named [bankName] (if it isn't blank). */
    val bank: Bank?,
    val number: String,
    /** The opening balance; blank means zero. */
    val initialBalance: String,
    val memo: String,
)

/** Why an [AccountInput] can't be saved. Each error is for a single field. */
enum class AccountInputError {
    BlankName,
    MissingAccountType,
    InvalidInitialBalance,
}

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

/**
 * The amount in [initialBalance], formatted for the default locale; zero if it's blank, or null if it
 * isn't a valid amount.
 */
fun parseInitialBalance(initialBalance: String): Money? =
    if (initialBalance.isBlank()) {
        Money(0)
    } else {
        try {
            Money.parse(initialBalance)
        } catch (_: NumberFormatException) {
            null
        } catch (_: ArithmeticException) {
            // Too large to fit in a Money.
            null
        }
    }
