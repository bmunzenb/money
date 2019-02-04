package com.munzenberger.money.core

import com.munzenberger.money.core.model.BankModel
import com.munzenberger.money.core.model.BankModelQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class Bank(executor: QueryExecutor, model: BankModel = BankModel()) : Persistable<BankModel>(model, BankModelQueryBuilder, executor) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, BankModelQueryBuilder, BankResultSetMapper(executor))

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, BankModelQueryBuilder, BankResultSetMapper(executor), Bank::class)
    }
}

class BankResultSetMapper(private val executor: QueryExecutor) : ResultSetMapper<Bank> {

    override fun map(resultSet: ResultSet): Bank {

        val model = BankModel().apply {
            identity = resultSet.getLong(BankModelQueryBuilder.identityColumn)
            name = resultSet.getString(BankModelQueryBuilder.nameColumn)
        }

        return Bank(executor, model)
    }
}
