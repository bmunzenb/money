package com.munzenberger.money.core

import com.munzenberger.money.core.model.PayeeModel
import com.munzenberger.money.core.model.PayeeModelQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class Payee(executor: QueryExecutor, model: PayeeModel = PayeeModel()) : Persistable<PayeeModel>(model, PayeeModelQueryBuilder, executor) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, PayeeModelQueryBuilder, PayeeResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, PayeeModelQueryBuilder, PayeeResultSetMapper(executor), Payee::class)
    }
}

class PayeeResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Payee> {

    override fun map(resultSet: ResultSet): Payee {

        val model = PayeeModel().apply {
            identity = resultSet.getLong(PayeeModelQueryBuilder.identityColumn)
            name = resultSet.getString(PayeeModelQueryBuilder.nameColumn)
        }

        return Payee(executor, model)
    }
}
