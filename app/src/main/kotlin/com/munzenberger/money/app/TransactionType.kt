package com.munzenberger.money.app

import com.munzenberger.money.core.AccountType

sealed class TransactionType {

    companion object {
        fun getTypes(accountType: AccountType) = listOf(Credit(accountType), Debit(accountType))
    }

    abstract val name: String

    class Credit(accountType: AccountType) : TransactionType() {
        override val name = when (accountType.variant) {
            AccountType.Variant.SAVINGS, AccountType.Variant.CHECKING -> "Deposit"
            AccountType.Variant.ASSET, AccountType.Variant.CASH -> "Increase"
            AccountType.Variant.CREDIT, AccountType.Variant.LOAN -> "Payment"
            AccountType.Variant.INCOME, AccountType.Variant.EXPENSE, null -> "Credit"
        }
    }

    class Debit(accountType: AccountType) : TransactionType() {
        override val name = when (accountType.variant) {
            AccountType.Variant.SAVINGS, AccountType.Variant.CHECKING -> "Withdrawal"
            AccountType.Variant.ASSET, AccountType.Variant.CASH -> "Decrease"
            AccountType.Variant.CREDIT -> "Charge"
            AccountType.Variant.LOAN -> "Borrow"
            AccountType.Variant.INCOME, AccountType.Variant.EXPENSE, null -> "Debit"
        }
    }

    object Split : TransactionType() {
        override val name = "Split"
    }
}
