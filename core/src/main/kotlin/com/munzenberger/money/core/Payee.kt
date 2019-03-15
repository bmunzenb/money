package com.munzenberger.money.core

import com.munzenberger.money.core.model.PayeeModel
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class Payee(model: PayeeModel = PayeeModel()) : Persistable<PayeeModel>(model, PayeeTable) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, PayeeTable, PayeeResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, PayeeTable, PayeeResultSetMapper(), Payee::class)
    }
}

class PayeeResultSetMapper : ResultSetMapper<Payee> {

    override fun map(resultSet: ResultSet): Payee {

        val model = PayeeModel().apply {
            identity = resultSet.getLong(PayeeTable.identityColumn)
            name = resultSet.getString(PayeeTable.nameColumn)
        }

        return Payee(model)
    }
}
