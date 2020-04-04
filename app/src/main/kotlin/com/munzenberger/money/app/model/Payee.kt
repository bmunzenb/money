package com.munzenberger.money.app.model

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.PayeeResultSetMapper
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetMapper
import com.munzenberger.money.sql.getLocalDateOrNull
import java.sql.ResultSet
import java.time.LocalDate

fun Payee.Companion.getAllWithLastPaid(database: MoneyDatabase): List<Pair<Payee, LocalDate?>> {

    val query = Query.selectFrom(PayeeTable.name)
            .cols(PayeeTable.identityColumn, PayeeTable.nameColumn, "MAX(${TransactionTable.dateColumn}) AS LAST_PAID")
            .leftJoin(PayeeTable.name, PayeeTable.identityColumn, TransactionTable.name, TransactionTable.payeeColumn)
            .groupBy(PayeeTable.identityColumn)
            .build()

    return database.getList(query, object : ResultSetMapper<Pair<Payee, LocalDate?>> {

        private val payeeMapper = PayeeResultSetMapper()

        override fun apply(rs: ResultSet): Pair<Payee, LocalDate?> {
            val payee = payeeMapper.apply(rs)
            val latePaid = rs.getLocalDateOrNull("LAST_PAID")

            return payee to latePaid
        }
    })
}
