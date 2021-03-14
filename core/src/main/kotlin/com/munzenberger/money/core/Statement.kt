package com.munzenberger.money.core

import com.munzenberger.money.core.model.StatementModel
import com.munzenberger.money.core.model.StatementTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet
import java.time.LocalDate

class Statement internal constructor(model: StatementModel) : Persistable<StatementModel>(model, StatementTable) {

    constructor() : this(StatementModel())

    private val accountRef = PersistableIdentityReference(model.account)

    fun setAccount(account: Account) {
        accountRef.set(account)
    }

    var closingDate: LocalDate?
        get() = model.closingDate?.let { LocalDate.ofEpochDay(it) }
        set(value) { model.closingDate = value?.toEpochDay() }

    var endingBalance: Money?
        get() = model.endingBalance?.let { Money.valueOf(it) }
        set(value) { model.endingBalance = value?.value }

    var isReconciled: Boolean?
        get() = model.isReconciled
        set(value) { model.isReconciled = value }

    override fun save(executor: QueryExecutor) = executor.transaction { tx ->
        model.account = accountRef.getIdentity(tx)
        super.save(executor)
    }

    companion object {

        fun getAll(executor: QueryExecutor) =
                getAll(executor, StatementTable, StatementResultSetMapper())

        fun get(identity: Long, executor: QueryExecutor) =
                get(identity, executor, StatementTable, StatementResultSetMapper())
    }
}

class StatementResultSetMapper : ResultSetMapper<Statement> {

    override fun apply(rs: ResultSet): Statement {

        val model = StatementModel().apply {
            StatementTable.getValues(rs, this)
        }

        return Statement(model)
    }
}
