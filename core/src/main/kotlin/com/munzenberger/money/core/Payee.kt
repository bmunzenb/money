package com.munzenberger.money.core

import com.munzenberger.money.core.model.PayeeModel
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class Payee internal constructor(model: PayeeModel) : AbstractPersistable<PayeeModel>(model, PayeeTable) {

    constructor() : this(PayeeModel())

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, PayeeTable, PayeeResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, PayeeTable, PayeeResultSetMapper())
    }
}

class PayeeResultSetMapper : ResultSetMapper<Payee> {

    override fun apply(resultSet: ResultSet): Payee {

        val model = PayeeModel().apply {
            PayeeTable.getValues(resultSet, this)
        }

        return Payee(model)
    }
}
