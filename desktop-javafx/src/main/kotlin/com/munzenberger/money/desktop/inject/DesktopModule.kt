package com.munzenberger.money.desktop.inject

import com.munzenberger.money.desktop.transactions.DefaultTransactionCategoryFormatter
import com.munzenberger.money.presentation.transactions.TransactionCategoryFormatter
import org.koin.dsl.module

/** The presentation layer's dependencies that this UI module implements, such as formatters using its strings. */
val desktopModule = module {
    single<TransactionCategoryFormatter> { DefaultTransactionCategoryFormatter() }
}
