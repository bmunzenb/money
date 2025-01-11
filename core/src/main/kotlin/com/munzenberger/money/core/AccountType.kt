package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeGroup
import com.munzenberger.money.core.model.AccountTypeModel
import com.munzenberger.money.core.model.AccountTypeTable
import com.munzenberger.money.core.model.AccountTypeVariant
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

data class AccountTypeIdentity(override val value: Long) : Identity

class AccountType internal constructor(model: AccountTypeModel) : AbstractMoneyEntity<AccountTypeIdentity, AccountTypeModel>(model, AccountTypeTable) {

    constructor() : this(AccountTypeModel())

    override val identity: AccountTypeIdentity?
        get() = model.identity?.let { AccountTypeIdentity(it) }

    var group: AccountTypeGroup?
        get() = model.group
        set(value) { model.group = value }

    var variant: AccountTypeVariant?
        get() = model.variant
        set(value) { model.variant = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, AccountTypeTable, AccountTypeResultSetMapper())

        fun get(identity: AccountTypeIdentity, executor: QueryExecutor) =
                get(identity, executor, AccountTypeTable, AccountTypeResultSetMapper())
    }
}

class AccountTypeResultSetMapper : ResultSetMapper<AccountType> {

    override fun apply(resultSet: ResultSet): AccountType {

        val model = AccountTypeModel().apply {
            AccountTypeTable.getValues(resultSet, this)
        }

        return AccountType(model)
    }
}
