package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeModel
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class AccountType(executor: QueryExecutor, model: AccountTypeModel = AccountTypeModel()) : Persistable<AccountTypeModel>(model, AccountTypeTable, executor) {

    enum class Category {
        ASSETS,
        LIABILITIES,
        INCOME,
        EXPENSES
    }

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    var category: Category?
        get() = model.category?.let { Category.valueOf(it) }
        set(value) { model.category = value?.name }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, AccountTypeTable, AccountTypeResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, AccountTypeTable, AccountTypeResultSetMapper(executor), AccountType::class)
    }
}

class AccountTypeResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<AccountType> {

    override fun map(resultSet: ResultSet): AccountType {

        val model = AccountTypeModel().apply {
            identity = resultSet.getLong(AccountTypeTable.identityColumn)
            name = resultSet.getString(AccountTypeTable.nameColumn)
            category = resultSet.getString(AccountTypeTable.categoryColumn)
        }

        return AccountType(executor, model)
    }
}
