package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.register.RegisterEntry

/**
 * The kind of account a register's two amount columns are named for. The register shows each entry's amount as a
 * positive value in one of two columns: [RegisterEntry.decrease] for amounts that lower the account's balance in the
 * repository, and [RegisterEntry.increase] for amounts that raise it, even for a liability, whose balance the register
 * shows as the amount owed (see [showsBalanceOwed]). The UI names the columns for the kind of account, such as
 * Payment and Deposit for a bank account, or Charge and Payment for a credit card.
 */
enum class RegisterAmountColumns {
    /** A checking, savings, or cash account. */
    Banking,
    CreditCard,
    Loan,

    /** Any other account, or one that hasn't loaded yet. */
    Other,
}

/** The amount columns for this account's type. */
val Account.registerAmountColumns: RegisterAmountColumns
    get() = when (accountType.value) {
        AccountTypeConstant.Checking, AccountTypeConstant.Savings, AccountTypeConstant.Cash ->
            RegisterAmountColumns.Banking
        AccountTypeConstant.Credit -> RegisterAmountColumns.CreditCard
        AccountTypeConstant.Loan -> RegisterAmountColumns.Loan
        AccountTypeConstant.Asset -> RegisterAmountColumns.Other
    }

/** How much this entry lowers the account's balance, as a positive amount, or null if it doesn't lower it. */
val RegisterEntry.decrease: Money?
    get() = if (amount.value < 0) -amount else null

/**
 * How much this entry raises the account's balance, or null if it lowers it. An entry of zero shows here, so that every
 * entry shows an amount.
 */
val RegisterEntry.increase: Money?
    get() = amount.takeIf { it.value >= 0 }
