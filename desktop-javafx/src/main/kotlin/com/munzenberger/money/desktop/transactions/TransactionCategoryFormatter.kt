package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.presentation.transactions.TransactionCategory

interface TransactionCategoryFormatter {
    fun format(category: TransactionCategory): String
}
