package com.munzenberger.money.app.model

import com.munzenberger.money.app.TransactionType
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountResultSetMapper
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.AccountTypeResultSetMapper
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq
import java.lang.UnsupportedOperationException

const val SPLIT_CATEGORY_NAME = "Split/Multiple Categories"

fun categoryName(accountName: String?, isCategory: Boolean?): String = when (isCategory) {
    true -> accountName ?: "<empty>"
    else -> "Transfer : $accountName"
}

interface DelayedCategory {

    val name: String

    val isTransfer: Boolean

    fun getCategory(executor: QueryExecutor, transactionType: TransactionType): Account

    companion object {

        fun from(account: Account): DelayedCategory = RealCategory(account)

        fun from(string: String): DelayedCategory = PendingCategory(string)

        fun split(): DelayedCategory = object : DelayedCategory {

            override val name = SPLIT_CATEGORY_NAME
            override val isTransfer = false

            override fun getCategory(executor: QueryExecutor, transactionType: TransactionType): Account {
                throw UnsupportedOperationException()
            }
        }
    }
}

private class RealCategory(private val account: Account) : DelayedCategory {

    override val name = account.categoryName
    override val isTransfer = account.accountType?.isCategory != true

    override fun getCategory(executor: QueryExecutor, transactionType: TransactionType) = account
}

private class PendingCategory(string: String) : DelayedCategory {

    override val name = string
    override val isTransfer: Boolean = false

    override fun getCategory(executor: QueryExecutor, transactionType: TransactionType): Account {

        var account = AccountTable
                .select()
                .where(AccountTable.nameColumn.eq(name))
                .build()
                .let { executor.getFirst(it, AccountResultSetMapper()) }

        if (account == null) {

            // TODO: should also use the sign of the amount to determine variant
            val variant = when (transactionType.variant) {
                TransactionType.Variant.CREDIT -> AccountType.Variant.INCOME
                TransactionType.Variant.DEBIT -> AccountType.Variant.EXPENSE
            }

            // find the account type that matches the transaction type
            val accountType = AccountTypeTable
                    .select()
                    .where(AccountTypeTable.variantColumn.eq(variant.name))
                    .build()
                    .let { executor.getFirst(it, AccountTypeResultSetMapper()) }

            account = Account().apply {
                this.name = this@PendingCategory.name
                this.accountType = accountType
                save(executor)
            }
        }

        return account
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
