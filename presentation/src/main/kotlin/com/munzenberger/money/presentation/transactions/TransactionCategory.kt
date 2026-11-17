package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.core.category.CategoryWithParent
import com.munzenberger.money.data.api.account.Account

/** What a transaction's entry can be assigned to: a category, or another account to transfer to or from. */
sealed interface TransactionCategory {
    data class Category(val category: CategoryWithParent) : TransactionCategory

    data class Transfer(val account: Account) : TransactionCategory
}
