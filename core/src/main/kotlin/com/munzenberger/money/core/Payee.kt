package com.munzenberger.money.core

import com.munzenberger.money.core.model.PayeeModel
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Single
import java.sql.ResultSet

class Payee internal constructor(model: PayeeModel) : Persistable<PayeeModel>(model, PayeeTable) {

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
            identity = resultSet.getLong(PayeeTable.identityColumn)
            name = resultSet.getString(PayeeTable.nameColumn)
        }

        return Payee(model)
    }
}

fun Payee.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Payee> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Payee::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Payee.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }
