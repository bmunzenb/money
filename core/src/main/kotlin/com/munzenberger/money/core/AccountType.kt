package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeModel
import com.munzenberger.money.core.model.AccountTypeTable
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

    var category: Category?
        get() = model.category?.let { Category.valueOf(it) }
        set(value) { model.category = value?.name }

    var variant: Variant?
        get() = model.variant?.let { Variant.valueOf(it) }
        set(value) { model.variant = value?.name }

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
            category = resultSet.getString(AccountTypeTable.categoryColumn)
            variant = resultSet.getString(AccountTypeTable.variantColumn)
        }

        return AccountType(model)
    }
}

fun AccountType.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<AccountType> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(AccountType::class, identity))
        else -> it.onSuccess(value)
    }
}

fun AccountType.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }
