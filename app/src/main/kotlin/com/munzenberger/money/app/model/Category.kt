package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType

const val CATEGORY_NAME_DELIMITER = ":"

const val SPLIT_CATEGORY_NAME = "Split/Multiple Categories"

fun categoryName(accountTypeCategory: AccountType.Category? = null, accountName: String?, categoryName: String? = null): String {
    return when {
        accountName == null -> "<error: no account name in category>"
        categoryName == null -> when (accountTypeCategory) {
            AccountType.Category.ASSETS, AccountType.Category.LIABILITIES -> "Transfer $CATEGORY_NAME_DELIMITER $accountName"
            else -> accountName
        }
        else -> "$accountName $CATEGORY_NAME_DELIMITER $categoryName"
    }
}
