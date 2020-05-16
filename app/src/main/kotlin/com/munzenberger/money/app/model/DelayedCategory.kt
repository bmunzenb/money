package com.munzenberger.money.app.model

import com.munzenberger.money.app.TransactionType
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountResultSetMapper
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.AccountTypeResultSetMapper
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.CategoryResultSetMapper
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.core.model.CategoryTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq
import com.munzenberger.money.sql.isNull
import java.lang.UnsupportedOperationException

interface DelayedCategory {

    val name: String

    val isTransfer: Boolean

    fun getCategory(executor: QueryExecutor, transactionType: TransactionType): Category

    companion object {

        fun from(category: Category): DelayedCategory = RealCategory(category)

        fun from(string: String): DelayedCategory = PendingCategory(string)

        fun split(): DelayedCategory = object : DelayedCategory {

            override val name = SPLIT_CATEGORY_NAME
            override val isTransfer: Boolean = false

            override fun getCategory(executor: QueryExecutor, transactionType: TransactionType): Category {
                throw UnsupportedOperationException()
            }
        }
    }
}

private class RealCategory(private val category: Category) : DelayedCategory {

    override val name: String
    override val isTransfer: Boolean

    init {

        val accountTypeCategory = category.account?.accountType?.category
        val accountName = category.account?.name
        val categoryName = category.name

        name = buildCategoryName(accountTypeCategory, accountName, categoryName)
        isTransfer = listOf(AccountType.Category.ASSETS, AccountType.Category.LIABILITIES).contains(accountTypeCategory)
    }

    override fun getCategory(executor: QueryExecutor, transactionType: TransactionType) = category
}

private class PendingCategory(string: String) : DelayedCategory {

    override val name: String
    override val isTransfer: Boolean = false

    private val accountName: String
    private val categoryName: String?

    init {

        val values = string.split(CATEGORY_NAME_DELIMITER, limit = 2)

        accountName = values[0].trim()
        categoryName = if (values.size > 1) values[1].trim() else null

        name = buildCategoryName(accountName = accountName, categoryName = categoryName)
    }

    override fun getCategory(executor: QueryExecutor, transactionType: TransactionType): Category {

        var category: Category?

        var account = AccountTable.select().where(AccountTable.nameColumn.eq(accountName)).build().let {
            executor.getFirst(it, AccountResultSetMapper())
        }

        when (account) {

            null -> {
                // TODO: should also use the sign of the amount to determine variant
                val variant = when (transactionType.variant) {
                    TransactionType.Variant.CREDIT -> AccountType.Variant.INCOME
                    TransactionType.Variant.DEBIT -> AccountType.Variant.EXPENSE
                }

                // find the account type that matches the transaction type
                val accountType = AccountTypeTable.select().where(AccountTypeTable.variantColumn.eq(variant.name)).build().let {
                    executor.getFirst(it, AccountTypeResultSetMapper())
                }

                account = Account().apply {
                    this.name = accountName
                    this.accountType = accountType
                    save(executor)
                }

                category = Category().apply {
                    this.account = account
                    save(executor)
                }
            }

            else -> {

                category = CategoryTable.select().where(CategoryTable.accountColumn.eq(account.identity!!).and(CategoryTable.nameColumn.isNull())).build().let {
                    executor.getFirst(it, CategoryResultSetMapper())
                }

                if (category == null) {
                    category = Category().apply {
                        this.account = account
                        save(executor)
                    }
                }
            }
        }

        if (categoryName != null) {

            val c = CategoryTable.select().where(CategoryTable.accountColumn.eq(account.identity!!).and(CategoryTable.nameColumn.eq(categoryName))).build().let {
                executor.getFirst(it, CategoryResultSetMapper())
            }

            category = when (c) {
                null -> Category().apply {
                    this.account = account
                    this.name = categoryName
                    save(executor)
                }
                else -> c
            }
        }

        return category
    }
}

object DelayedCategoryComparator : Comparator<DelayedCategory> {
    override fun compare(o1: DelayedCategory?, o2: DelayedCategory?): Int =
            when {
                o1 == o2 -> 0
                o1 == null -> 1
                o2 == null -> -1
                o1.isTransfer && !o2.isTransfer -> 1
                o2.isTransfer && !o1.isTransfer -> -1
                else -> o1.name.compareTo(o2.name)
            }
}
