package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeModel
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.sql.Condition
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Single
import java.sql.ResultSet

class AccountType internal constructor(model: AccountTypeModel) : Persistable<AccountTypeModel>(model, AccountTypeTable) {

    constructor() : this(AccountTypeModel())

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
                Persistable.getAll(executor, AccountTypeTable, AccountTypeResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, AccountTypeTable, AccountTypeResultSetMapper(), AccountType::class)

        fun getAllForCategories(executor: QueryExecutor, vararg categories: Category) = Single.fromCallable {

            val condition = Condition.inGroup(AccountTypeTable.categoryColumn, categories.map { it.name })
            val query = AccountTypeTable.select().where(condition).build()

            executor.getList(query, AccountTypeResultSetMapper())
        }
    }
}

class AccountTypeResultSetMapper : ResultSetMapper<AccountType> {

    override fun apply(resultSet: ResultSet): AccountType {

        val model = AccountTypeModel().apply {
            identity = resultSet.getLong(AccountTypeTable.identityColumn)
            name = resultSet.getString(AccountTypeTable.nameColumn)
            category = resultSet.getString(AccountTypeTable.categoryColumn)
        }

        return AccountType(model)
    }
}
