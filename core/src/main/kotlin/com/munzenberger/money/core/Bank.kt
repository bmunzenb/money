package com.munzenberger.money.core

import com.munzenberger.money.core.model.BankModel
import com.munzenberger.money.core.model.BankTable
import com.munzenberger.money.sql.OrderableQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

data class BankIdentity(
    override val value: Long,
) : Identity

class Bank internal constructor(
    model: BankModel,
) : AbstractMoneyEntity<BankIdentity, BankModel>(model, BankTable) {
    constructor() : this(BankModel())

    override val identity: BankIdentity?
        get() = model.identity?.let { BankIdentity(it) }

    var name: String?
        get() = model.name
        set(value) {
            model.name = value
        }

    companion object {
        fun find(
            executor: QueryExecutor,
            block: OrderableQueryBuilder<*>.() -> Unit = {},
        ) = find(executor, BankTable, BankResultSetMapper, block)

        fun get(
            identity: BankIdentity,
            executor: QueryExecutor,
        ) = get(identity, executor, BankTable, BankResultSetMapper)
    }
}

object BankResultSetMapper : ResultSetMapper<Bank> {
    override fun apply(resultSet: ResultSet): Bank {
        val model = BankTable.getValues(resultSet, BankModel())
        return Bank(model)
    }
}
