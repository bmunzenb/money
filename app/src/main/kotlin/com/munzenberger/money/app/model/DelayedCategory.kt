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

class RealCategory(val category: Category) : DelayedCategory {

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

    fun toRealCategory(executor: QueryExecutor, transactionType: TransactionType): RealCategory {

        lateinit var category: Category

        var account = AccountTable.select().where(AccountTable.nameColumn.eq(accountName)).build().let {
            executor.getFirst(it, AccountResultSetMapper())
        }

        if (account == null) {

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
            }

            category = Category().apply {
                this.account = account
                save(executor)
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

        return RealCategory(category)
    }
}

object SplitCategory : DelayedCategory {

    override val name = "Split"
}
