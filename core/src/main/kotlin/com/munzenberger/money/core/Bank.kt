package com.munzenberger.money.core

import com.munzenberger.money.core.model.BankModel
import com.munzenberger.money.core.model.BankModelQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Single
import java.sql.ResultSet

class Bank(executor: QueryExecutor, model: BankModel = BankModel()) : Persistable<BankModel>(model, BankModelQueryBuilder, executor) {

    var name: String?
        get() = model.name
        set(value) { model.name = value }

    companion object {

        fun getAll(executor: QueryExecutor) = Single.create<List<Bank>> {

            val query = BankModelQueryBuilder.select()
            val list = executor.getList(query, BankResultSetMapper(executor))

            it.onSuccess(list)
        }

        fun get(identity: Long, executor: QueryExecutor) = Single.create<Bank> {

            val query = BankModelQueryBuilder.select(identity)
            val bank = executor.getFirst(query, BankResultSetMapper(executor))

            when (bank) {
                is Bank -> it.onSuccess(bank)
                else -> it.onError(PersistableNotFoundException(Bank::class, identity))
            }
        }
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
