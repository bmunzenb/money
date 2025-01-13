package com.munzenberger.money.app.model

import com.munzenberger.money.app.TransactionType
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.model.CategoryType
import com.munzenberger.money.sql.QueryExecutor

const val SPLIT_CATEGORY_NAME = "Split/Multiple Categories"
const val CATEGORY_DELIMITER = ":"

sealed class TransactionCategory {
    abstract val name: String

    class TransferType(
        val account: Account,
    ) : TransactionCategory() {
        override val name = "Transfer $CATEGORY_DELIMITER ${account.name}"
    }

    class CategoryType(
        val category: Category,
        parentName: String?,
    ) : TransactionCategory() {
        override val name =
            when (parentName) {
                null -> "${category.name}"
                else -> "$parentName $CATEGORY_DELIMITER ${category.name}"
            }
    }

    class Pending(
        string: String,
    ) : TransactionCategory() {
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

        override val name =
            when (parentName) {
                null -> categoryName
                else -> "$parentName $CATEGORY_DELIMITER $categoryName"
            }

        fun getCategory(
            executor: QueryExecutor,
            isNegative: Boolean?,
            transactionType: TransactionType?,
        ): Category {
            val parentCategory =
                parentName?.let {
                    val c =
                        Category
                            .find(
                                executor,
                                name = it,
                                isParent = true,
                            ).firstOrNull()

                    c ?: Category().apply {
                        this.name = parentName
                        this.type = transactionType.categoryType(isNegative)
                        save(executor)
                    }
                }

            val category =
                Category
                    .find(
                        executor,
                        name = categoryName,
                        isParent = parentName == null,
                        parentId = parentCategory?.identity,
                    ).firstOrNull()

            return category ?: Category().apply {
                this.name = categoryName
                this.type = transactionType.categoryType(isNegative)
                this.setParent(parentCategory)
                save(executor)
            }
        }
    }

    object Split : TransactionCategory() {
        override val name = SPLIT_CATEGORY_NAME
    }
}

private fun TransactionType?.categoryType(isNegative: Boolean?): CategoryType {
    val type =
        when (this?.variant) {
            TransactionType.Variant.CREDIT -> CategoryType.INCOME
            else -> CategoryType.EXPENSE
        }

    return when {
        isNegative == true && type == CategoryType.INCOME -> CategoryType.EXPENSE
        isNegative == true && type == CategoryType.EXPENSE -> CategoryType.INCOME
        else -> type
    }
}
