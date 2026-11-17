package com.munzenberger.money.presentation.transactions

/**
 * Formats a [TransactionCategory] as the UI shows it. The UI module supplies the implementation, since the text comes
 * from its string resources.
 */
fun interface TransactionCategoryFormatter {
    fun format(category: TransactionCategory): String
}
