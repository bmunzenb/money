package com.munzenberger.money.core.account

import com.munzenberger.money.data.api.Money
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
