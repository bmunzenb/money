package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeModel
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class AccountType internal constructor(model: AccountTypeModel) : Persistable<AccountTypeModel>(model, AccountTypeTable) {

    constructor() : this(AccountTypeModel())

    enum class Group {
        ASSETS,
        LIABILITIES,
        INCOME,
        EXPENSES
    }

    enum class Variant {
        SAVINGS,
        CHECKING,
        ASSET,
        CASH,
        CREDIT,
        LOAN,
        INCOME,
        EXPENSE
    }

    var group: Group?
        get() = model.group?.let { Group.valueOf(it) }
        set(value) { model.group = value?.name }

    var variant: Variant?
        get() = model.variant?.let { Variant.valueOf(it) }
        set(value) { model.variant = value?.name }

    var isCategory: Boolean?
        get() = model.isCategory
        set(value) { model.isCategory = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, AccountTypeTable, AccountTypeResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, AccountTypeTable, AccountTypeResultSetMapper())
    }
}

class AccountTypeResultSetMapper : ResultSetMapper<AccountType> {

    override fun apply(resultSet: ResultSet): AccountType {

        val model = AccountTypeModel().apply {
            identity = resultSet.getLong(AccountTypeTable.identityColumn)
            group = resultSet.getString(AccountTypeTable.groupColumn)
            variant = resultSet.getString(AccountTypeTable.variantColumn)
            isCategory = resultSet.getBoolean(AccountTypeTable.isCategoryColumn)
        }

        return AccountType(model)
    }
}
