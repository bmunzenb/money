package com.munzenberger.money.core

import com.munzenberger.money.core.model.BankModel
import com.munzenberger.money.core.model.BankTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Single
import java.sql.ResultSet

class Bank internal constructor(model: BankModel) : Persistable<BankModel>(model, BankTable) {

    constructor() : this(BankModel())

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, BankTable, BankResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, BankTable, BankResultSetMapper())
    }
}

class BankResultSetMapper : ResultSetMapper<Bank> {

    override fun apply(resultSet: ResultSet): Bank {

        val model = BankModel().apply {
            identity = resultSet.getLong(BankTable.identityColumn)
            name = resultSet.getString(BankTable.nameColumn)
        }

        return Bank(model)
    }
}

fun Bank.Companion.observableGet(identity: Long, executor: QueryExecutor) = Single.create<Bank> {
    when (val value = get(identity, executor)) {
        null -> it.onError(PersistableNotFoundException(Bank::class, identity))
        else -> it.onSuccess(value)
    }
}

fun Bank.Companion.observableGetAll(executor: QueryExecutor) = Single.fromCallable { getAll(executor) }
