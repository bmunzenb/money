package com.munzenberger.money.app.model

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransferResultSetMapper
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.eq
import io.reactivex.Single

fun Transaction.getTransfers(database: MoneyDatabase) = Single.fromCallable {

    when (val id = identity) {
        null ->
            emptyList()

        else -> {
            val query = TransferTable.select().where(TransferTable.transactionColumn.eq(id)).build()
            database.getList(query, TransferResultSetMapper())
        }
    }
}
