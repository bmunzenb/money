package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Category

const val SPLIT_CATEGORY_NAME = "Split/Multiple Categories"
const val CATEGORY_DELIMITER = ":"

sealed class DelayedCategory {

    abstract val name: String

    class Transfer(val account: Account) : DelayedCategory() {

        override val name = "Transfer $CATEGORY_DELIMITER ${account.name}"
    }

    class Entry(val category: Category, parentName: String?) : DelayedCategory() {

        override val name = when (parentName) {
            null -> "${category.name}"
            else -> "$parentName $CATEGORY_DELIMITER ${category.name}"
        }
    }

    class Pending(string: String) : DelayedCategory() {

        private val categoryName: String
        private val parentName: String?

        init {
            string.split(CATEGORY_DELIMITER, limit = 2).let {
                when (it.size) {
                    1 -> {
                        categoryName = it[0].trim()
                        parentName = null
                    }
                    else -> {
                        categoryName = it[1].trim()
                        parentName = it[0].trim()
                    }
                }
            }
        }

        override val name = when (parentName) {
            null -> categoryName
            else -> "$parentName $CATEGORY_DELIMITER $categoryName"
        }
    }

    object Split : DelayedCategory() {
        override val name = SPLIT_CATEGORY_NAME
    }
}
