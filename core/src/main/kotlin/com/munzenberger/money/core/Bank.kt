package com.munzenberger.money.core

import com.munzenberger.money.core.model.BankModel
import com.munzenberger.money.core.model.BankTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

data class BankIdentity(override val value: Long) : Identity

class Bank internal constructor(model: BankModel) : AbstractMoneyEntity<BankIdentity, BankModel>(model, BankTable) {
    constructor() : this(BankModel())

    override val identity: BankIdentity?
        get() = model.identity?.let { BankIdentity(it) }

    var name: String?
        get() = model.name
        set(value) {
            model.name = value
        }

    companion object {
        fun getAll(executor: QueryExecutor) = getAll(executor, BankTable, BankResultSetMapper())

        fun get(
            identity: BankIdentity,
            executor: QueryExecutor,
        ) = get(identity, executor, BankTable, BankResultSetMapper())
    }
}

class BankResultSetMapper : ResultSetMapper<Bank> {
    override fun apply(resultSet: ResultSet): Bank {
        val model =
            BankModel().apply {
                BankTable.getValues(resultSet, this)
            }

        return Bank(model)
    }
}
