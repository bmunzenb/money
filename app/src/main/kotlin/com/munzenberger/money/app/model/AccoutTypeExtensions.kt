package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.AccountTypeResultSetMapper
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.inGroup
import io.reactivex.Single

fun AccountType.Companion.getForCategories(database: MoneyDatabase, vararg categories: AccountType.Category) = Single.fromCallable {

    val query = AccountTypeTable.select()
            .where(AccountTypeTable.categoryColumn.inGroup(categories.map { it.name }))
            .orderBy(AccountTypeTable.identityColumn)
            .build()

    database.getList(query, AccountTypeResultSetMapper())
}
