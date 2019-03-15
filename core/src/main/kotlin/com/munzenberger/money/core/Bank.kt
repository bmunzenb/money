package com.munzenberger.money.core

import com.munzenberger.money.core.model.BankModel
import com.munzenberger.money.core.model.BankTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import java.sql.ResultSet

class Bank(model: BankModel = BankModel()) : Persistable<BankModel>(model, BankTable) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                Persistable.getAll(executor, BankTable, BankResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                Persistable.get(identity, executor, BankTable, BankResultSetMapper(), Bank::class)
    }
}

class BankResultSetMapper : ResultSetMapper<Bank> {

    override fun map(resultSet: ResultSet): Bank {

        val model = BankModel().apply {
            identity = resultSet.getLong(BankTable.identityColumn)
            name = resultSet.getString(BankTable.nameColumn)
        }

        return Bank(model)
    }
}
