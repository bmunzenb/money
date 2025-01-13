package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountIdentity
import com.munzenberger.money.core.Statement
import com.munzenberger.money.core.model.StatementTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq

fun Statement.Companion.getUnreconciled(
    accountId: AccountIdentity,
    executor: QueryExecutor,
): Statement? {
    return findFirst(executor) {
        where(StatementTable.STATEMENT_ACCOUNT_ID.eq(accountId.value) and StatementTable.STATEMENT_IS_RECONCILED.eq(false))
        orderBy(StatementTable.STATEMENT_CLOSING_DATE)
    }
}
