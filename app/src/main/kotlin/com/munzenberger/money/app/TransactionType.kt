package com.munzenberger.money.app

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.model.AccountTypeVariant

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
            AccountTypeVariant.SAVINGS, AccountTypeVariant.CHECKING -> "Deposit"
            AccountTypeVariant.ASSET, AccountTypeVariant.CASH -> "Increase"
            else -> "Credit"
        }
    }

    private class Debit(accountType: AccountType) : TransactionType() {

        override val variant: Variant = Variant.DEBIT

        override val name = when (accountType.variant) {
            AccountTypeVariant.SAVINGS, AccountTypeVariant.CHECKING -> "Payment"
            AccountTypeVariant.ASSET, AccountTypeVariant.CASH -> "Decrease"
            AccountTypeVariant.CREDIT -> "Charge"
            else -> "Debit"
        }
    }
}

fun Money.forTransactionType(transactionType: TransactionType?): Money {
    return when (transactionType?.variant) {
        TransactionType.Variant.DEBIT -> negate()
        else -> this
    }
}
