package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountResultSetMapper
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.Condition
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq

private fun getAccounts(
        executor: QueryExecutor,
        isCategory: Boolean? = null
) : List<Account> {

    var condition: Condition? = null

    isCategory?.let {
        condition = AccountTypeTable.isCategoryColumn.eq(it)
    }

    // add additional filters here...
    // filter?.let {
    //     condition = Condition(it) and condition
    // }

    var builder = AccountTable.select()

    condition?.let {
        builder = builder.where(it)
    }

    builder = builder.orderBy(AccountTable.identityColumn)

    return executor.getList(builder.build(), AccountResultSetMapper())
}

fun Account.Companion.getAssetsAndLiabilities(database: MoneyDatabase) =
        getAccounts(database, isCategory = false)

fun Account.Companion.getCategories(database: MoneyDatabase) =
        getAccounts(database, isCategory = true)

val Account.categoryName: String
    get() = categoryName(name, accountType?.isCategory)
