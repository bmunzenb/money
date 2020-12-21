package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account

const val SPLIT_CATEGORY_NAME = "Split/Multiple Categories"
const val CATEGORY_DELIMITER = ":"

sealed class DelayedCategory {

    abstract val name: String

    class Transfer(val account: Account) : DelayedCategory() {

        override val name = "Transfer $CATEGORY_DELIMITER ${account.name}"
    }

    class Category(val category: com.munzenberger.money.core.Category, parentName: String?) : DelayedCategory() {

        override val name = when (parentName) {
            null -> category.name ?: ""
            else -> "$parentName $CATEGORY_DELIMITER ${category.name}"
        }
    }

    class Pending(string: String) : DelayedCategory() {
        override val name = string
    }

    object Split : DelayedCategory() {
        override val name = SPLIT_CATEGORY_NAME
    }
}
