package com.munzenberger.money.app.model

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountResultSetMapper
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.AccountTable
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.inGroup

fun Account.Companion.getAssetsAndLiabilities(database: MoneyDatabase): List<Account> {

    val categories = listOf(AccountType.Category.ASSETS, AccountType.Category.LIABILITIES).map { it.name }

    val query = AccountTable.select()
            .where(AccountTypeTable.categoryColumn.inGroup(categories))
            .orderBy(AccountTable.identityColumn)
            .build()

    return database.getList(query, AccountResultSetMapper())
}

