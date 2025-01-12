package com.munzenberger.money.core

import com.munzenberger.money.core.model.PayeeModel
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

data class PayeeIdentity(override val value: Long) : Identity

class Payee internal constructor(model: PayeeModel) : AbstractMoneyEntity<PayeeIdentity, PayeeModel>(model, PayeeTable) {
    constructor() : this(PayeeModel())

    override val identity: PayeeIdentity?
        get() = model.identity?.let { PayeeIdentity(it) }

    var name: String?
        get() = model.name
        set(value) {
            model.name = value
        }

    companion object {
        fun getAll(executor: QueryExecutor) = getAll(executor, PayeeTable, PayeeResultSetMapper())

        fun get(
            identity: PayeeIdentity,
            executor: QueryExecutor,
        ) = get(identity, executor, PayeeTable, PayeeResultSetMapper())
    }
}

class PayeeResultSetMapper : ResultSetMapper<Payee> {
    override fun apply(resultSet: ResultSet): Payee {
        val model =
            PayeeModel().apply {
                PayeeTable.getValues(resultSet, this)
            }

        return Payee(model)
    }
}
