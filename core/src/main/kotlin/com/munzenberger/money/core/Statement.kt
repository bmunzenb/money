package com.munzenberger.money.core

import com.munzenberger.money.core.model.StatementModel
import com.munzenberger.money.core.model.StatementTable
import com.munzenberger.money.sql.OrderableQueryBuilder
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.transaction
import java.sql.ResultSet
import java.time.LocalDate

data class StatementIdentity(override val value: Long) : Identity

class Statement internal constructor(model: StatementModel) : AbstractMoneyEntity<StatementIdentity, StatementModel>(
    model,
    StatementTable,
) {
    constructor() : this(StatementModel())

    private val accountRef = IdentityReference(model.account?.let { AccountIdentity(it) })

    override val identity: StatementIdentity?
        get() = model.identity?.let { StatementIdentity(it) }

    fun setAccount(account: Account) {
        accountRef.set(account)
    }

    var closingDate: LocalDate?
        get() = model.closingDate?.let { LocalDate.ofEpochDay(it) }
        set(value) {
            model.closingDate = value?.toEpochDay()
        }

    var startingBalance: Money?
        get() = model.startingBalance?.let { Money.valueOf(it) }
        set(value) {
            model.startingBalance = value?.value
        }

    var endingBalance: Money?
        get() = model.endingBalance?.let { Money.valueOf(it) }
        set(value) {
            model.endingBalance = value?.value
        }

    var isReconciled: Boolean?
        get() = model.isReconciled
        set(value) {
            model.isReconciled = value
        }

    override fun save(executor: QueryExecutor) =
        executor.transaction { tx ->
            model.account = accountRef.getAutoSavedIdentity(tx)?.value
            super.save(tx)
        }

    companion object {
        fun find(
            executor: QueryExecutor,
            block: OrderableQueryBuilder<*>.() -> Unit = {},
        ) = MoneyEntity.find(executor, StatementTable, StatementResultSetMapper, block)

        fun get(
            identity: StatementIdentity,
            executor: QueryExecutor,
        ) = MoneyEntity.get(identity, executor, StatementTable, StatementResultSetMapper)
    }
}

object StatementResultSetMapper : ResultSetMapper<Statement> {
    override fun apply(rs: ResultSet): Statement {
        val model =
            StatementModel().apply {
                StatementTable.getValues(rs, this)
            }

        return Statement(model)
    }
}
