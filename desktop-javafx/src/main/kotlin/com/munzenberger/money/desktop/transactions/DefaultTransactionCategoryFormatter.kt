package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.getString
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.transactions.TransactionCategory

/** Formats a category or transfer the same way the account register shows an entry's. */
class DefaultTransactionCategoryFormatter : TransactionCategoryFormatter {
    override fun format(category: TransactionCategory): String = when (category) {
        is TransactionCategory.Category -> {
            val name = category.category.category.name
            category.category.parent
                ?.let { strings.getString("register_subcategory_value", it.name, name) }
                ?: name
        }
        is TransactionCategory.Transfer -> strings.getString("register_transfer_value", category.account.name)
    }
}
