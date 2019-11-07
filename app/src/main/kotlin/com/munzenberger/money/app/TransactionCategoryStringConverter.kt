package com.munzenberger.money.app

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.MoneyDatabase
import javafx.util.StringConverter

class TransactionCategoryStringConverter : StringConverter<Category>() {

    lateinit var database: MoneyDatabase

    private val cache = mutableMapOf<Category, String>()

    override fun toString(category: Category?): String {

        if (category == null) {
            return ""
        }

        if (cache.containsKey(category)) {
            return cache[category]!!
        }

        val accountType = category.account?.accountType?.category
        val accountName = category.account?.name
        val categoryName = category.name

        val string = when {
            accountName == null -> "error: no account specified"
            categoryName == null -> when (accountType) {
                AccountType.Category.ASSETS, AccountType.Category.LIABILITIES -> "Transfer : $accountName"
                else -> accountName
            }
            else -> "$accountName : $categoryName"
        }

        cache[category] = string

        return string
    }

    override fun fromString(string: String?): Category {
        return Category()
    }
}
