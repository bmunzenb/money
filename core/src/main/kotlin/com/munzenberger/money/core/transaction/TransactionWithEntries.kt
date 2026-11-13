package com.munzenberger.money.core.transaction

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionEntry

data class TransactionWithEntries(
    val transaction: Transaction,
    val entries: List<TransactionEntry>
) {
    /**
     * How much the transaction changes its account's balance by: the sum of its entries' amounts, or zero if it has
     * none.
     *
     * @throws IllegalArgumentException if its entries are in different currencies.
     */
    val amount: Money
        // A reduce rather than a fold from Money(0), whose default currency would fail to add to entries in another
        // currency; this starts from the first entry, keeping the entries' currency.
        get() = entries.map { it.amount }.reduceOrNull(Money::plus) ?: Money(0)
}
