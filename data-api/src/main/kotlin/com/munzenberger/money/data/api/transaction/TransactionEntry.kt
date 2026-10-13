package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.Money

interface TransactionEntry {
    val transactionId: TransactionId

    /**
     * The amount this entry changes the balance of the parent transaction's account by: positive
     * adds to it (e.g. a deposit or a refund), negative subtracts from it (e.g. a payment). The
     * sign doesn't depend on the category's type, so a refund in an expense category is positive.
     *
     * For a [TransferEntry], the account it transfers to or from changes by the negation of this
     * amount.
     */
    val amount: Money
    val memo: String?
    val orderInTransaction: Int
}
