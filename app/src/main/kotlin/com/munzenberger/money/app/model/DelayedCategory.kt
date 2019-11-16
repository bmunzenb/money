package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Category

private const val delimiter = ":"

interface DelayedCategory {

    val name: String

    companion object {

        fun name(accountTypeCategory: AccountType.Category? = null, accountName: String?, categoryName: String?): String {
            return when {
                accountName == null -> "error: no account in category"
                categoryName == null -> when (accountTypeCategory) {
                    AccountType.Category.ASSETS, AccountType.Category.LIABILITIES -> "Transfer $delimiter $accountName"
                    else -> accountName
                }
                else -> "$accountName $delimiter $categoryName"
            }
        }
    }
}

class RealCategory(category: Category) : DelayedCategory {

    override val name: String

    init {

        val accountTypeCategory = category.account?.accountType?.category
        val accountName = category.account?.name
        val categoryName = category.name

        name = DelayedCategory.name(accountTypeCategory, accountName, categoryName)
    }
}

class PendingCategory(string: String) : DelayedCategory {

    override val name: String

    private val accountName: String
    private val categoryName: String?

    init {

        val values = string.split(delimiter, limit = 2)

        accountName = values[0].trim()
        categoryName = if (values.size > 1) values[1].trim() else null

        name = DelayedCategory.name(accountName = accountName, categoryName = categoryName)
    }
}

object SplitCategory : DelayedCategory {

    override val name = "Split"
}
