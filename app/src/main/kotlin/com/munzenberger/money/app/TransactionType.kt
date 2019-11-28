package com.munzenberger.money.app

import com.munzenberger.money.core.AccountType

sealed class TransactionType {

    enum class Variant {
        CREDIT, DEBIT
    }

    companion object {
        fun getTypes(accountType: AccountType) = listOf(Credit(accountType), Debit(accountType))
    }

    abstract val variant: Variant
    abstract val name: String

    private class Credit(accountType: AccountType) : TransactionType() {

        override val variant: Variant = Variant.CREDIT

        override val name = when (accountType.variant) {
            AccountType.Variant.SAVINGS, AccountType.Variant.CHECKING -> "Deposit"
            AccountType.Variant.ASSET, AccountType.Variant.CASH -> "Increase"
            else -> "Credit"
        }
    }

    private class Debit(accountType: AccountType) : TransactionType() {

        override val variant: Variant = Variant.DEBIT

        override val name = when (accountType.variant) {
            AccountType.Variant.SAVINGS, AccountType.Variant.CHECKING -> "Payment"
            AccountType.Variant.ASSET, AccountType.Variant.CASH -> "Decrease"
            AccountType.Variant.CREDIT -> "Charge"
            else -> "Debit"
        }
    }
}
