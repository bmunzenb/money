package com.munzenberger.money.app.model

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import com.munzenberger.money.core.PayeeResultSetMapper
import com.munzenberger.money.core.model.PayeeTable
import com.munzenberger.money.core.model.TransactionTable
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetMapper
import io.reactivex.Single
import java.sql.ResultSet
import java.util.Date

fun Payee.Companion.getAllWithLastPaid(database: MoneyDatabase): List<Pair<Payee, Date?>> {

    val query = Query.selectFrom(PayeeTable.name)
            .cols(PayeeTable.identityColumn, PayeeTable.nameColumn, "MAX(${TransactionTable.dateColumn}) AS LAST_PAID")
            .leftJoin(PayeeTable.name, PayeeTable.identityColumn, TransactionTable.name, TransactionTable.payeeColumn)
            .groupBy(PayeeTable.identityColumn)
            .build()

    return database.getList(query, object : ResultSetMapper<Pair<Payee, Date?>> {

        private val payeeMapper = PayeeResultSetMapper()

        override fun apply(rs: ResultSet): Pair<Payee, Date?> {
            val payee = payeeMapper.apply(rs)
            val latePaid: Date? = rs.getDate("LAST_PAID")

            return payee to latePaid
        }
    })
}
