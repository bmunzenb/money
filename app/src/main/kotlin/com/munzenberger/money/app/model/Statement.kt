package com.munzenberger.money.app.model

import com.munzenberger.money.core.Statement
import com.munzenberger.money.core.StatementResultSetMapper
import com.munzenberger.money.core.model.StatementTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq

fun Statement.Companion.getUnreconciled(accountId: Long, executor: QueryExecutor): Statement? {

    val query = StatementTable.select {
        where(StatementTable.accountColumn.eq(accountId) and StatementTable.isReconciledColumn.eq(false))
        orderBy(StatementTable.closingDateColumn)
    }

    return executor.getFirst(query, StatementResultSetMapper())
}
