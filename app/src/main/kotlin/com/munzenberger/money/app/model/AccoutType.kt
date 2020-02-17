package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.AccountTypeResultSetMapper
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.inGroup

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

fun AccountType.Companion.getForCategories(database: MoneyDatabase, vararg categories: AccountType.Category): List<AccountType> {

    val query = AccountTypeTable.select()
            .where(AccountTypeTable.categoryColumn.inGroup(categories.map { it.name }))
            .orderBy(AccountTypeTable.identityColumn)
            .build()

    return database.getList(query, AccountTypeResultSetMapper())
}
