package com.munzenberger.money.app.model

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.PayeeResultSetMapper
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.core.model.leftJoin
import com.munzenberger.money.sql.getLocalDateOrNull
import com.munzenberger.money.sql.selectQuery
import java.time.LocalDate

fun Payee.Companion.getAllWithLastPaid(database: MoneyDatabase): List<Pair<Payee, LocalDate?>> {
    val query =
        selectQuery(PayeeTable.tableName) {
            cols(PayeeTable.identityColumn, PayeeTable.PAYEE_NAME, "MAX(${TransactionTable.TRANSACTION_DATE}) AS LAST_PAID")
            leftJoin(PayeeTable, PayeeTable.identityColumn, TransactionTable, TransactionTable.TRANSACTION_PAYEE_ID)
            groupBy(PayeeTable.identityColumn)
        }

    return database.getList(
        query,
    ) { rs ->
        val payee = PayeeResultSetMapper.apply(rs)
        val latePaid = rs.getLocalDateOrNull("LAST_PAID")

        payee to latePaid
    }
}
