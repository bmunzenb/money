package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.AccountTypeResultSetMapper
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.Condition
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq

val AccountType.name: String?
    get() = when (variant) {
        AccountType.Variant.SAVINGS -> "Savings"
        AccountType.Variant.CHECKING -> "Checking"
        AccountType.Variant.ASSET -> "Asset"
        AccountType.Variant.CASH -> "Cash"
        AccountType.Variant.CREDIT -> "Credit Card"
        AccountType.Variant.LOAN -> "Loan"
        AccountType.Variant.INCOME -> "Income"
        AccountType.Variant.EXPENSE -> "Expense"
        null -> null
    }

private fun getAccountTypes(executor: QueryExecutor, isCategory: Boolean?): List<AccountType> {

    var condition: Condition? = null

    isCategory?.let {
        condition = AccountTypeTable.isCategoryColumn.eq(it)
    }

    var builder = AccountTypeTable.select()

    condition?.let {
        builder = builder.where(it)
    }

    builder = builder.orderBy(AccountTypeTable.identityColumn)

    return executor.getList(builder.build(), AccountTypeResultSetMapper())
}

fun AccountType.Companion.getForAccounts(executor: QueryExecutor): List<AccountType> =
        getAccountTypes(executor, isCategory = false)

fun AccountType.Companion.getForCategories(executor: QueryExecutor): List<AccountType> =
        getAccountTypes(executor, isCategory = true)
