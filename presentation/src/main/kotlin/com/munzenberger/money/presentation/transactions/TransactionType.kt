package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.data.api.Money

/** Whether a transaction raises or lowers its account's balance. */
enum class TransactionType {
    Increase,
    Decrease,
    ;

    companion object {
        /**
         * The type of a transaction that changes its account's balance by [amount]: [Decrease] if it lowers it, or
         * [Increase] otherwise, including for zero, as in the register's amount columns.
         */
        fun of(amount: Money): TransactionType = if (amount.value < 0) Decrease else Increase
    }
}
