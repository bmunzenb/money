package com.munzenberger.money.app

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.model.AccountTypeVariant

sealed class TransactionType {

    enum class Variant {
        CREDIT, DEBIT
    }

    abstract val variant: Variant
    abstract val name: String

    class Credit(accountType: AccountType?) : TransactionType() {
        override val variant = Variant.CREDIT
        override val name = when (accountType?.variant) {
            AccountTypeVariant.SAVINGS, AccountTypeVariant.CHECKING -> "Deposit"
            AccountTypeVariant.ASSET, AccountTypeVariant.CASH -> "Increase"
            AccountTypeVariant.LOAN -> "Payment"
            else -> "Credit"
        }
    }

    class Debit(accountType: AccountType?) : TransactionType() {
        override val variant = Variant.DEBIT
        override val name = when (accountType?.variant) {
            AccountTypeVariant.SAVINGS, AccountTypeVariant.CHECKING -> "Payment"
            AccountTypeVariant.ASSET, AccountTypeVariant.CASH -> "Decrease"
            AccountTypeVariant.CREDIT -> "Charge"
            AccountTypeVariant.LOAN -> "Increase"
            else -> "Debit"
        }
    }

    companion object {
        fun getTypes(accountType: AccountType) = listOf(Credit(accountType), Debit(accountType))
    }
}

fun Money.forTransactionType(transactionType: TransactionType?): Money {
    return forTransactionType(transactionType?.variant)
}

fun Money.forTransactionType(variant: TransactionType.Variant?): Money {
    return when (variant) {
        TransactionType.Variant.DEBIT -> negate()
        else -> this
    }
}

fun Money.forTransferType(transactionType: TransactionType?): Money {
    return forTransferType(transactionType?.variant)
}

fun Money.forTransferType(variant: TransactionType.Variant?): Money {
    return when (variant) {
        TransactionType.Variant.CREDIT -> negate()
        else -> this
    }
}
