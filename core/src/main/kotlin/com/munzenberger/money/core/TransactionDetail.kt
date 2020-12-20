package com.munzenberger.money.core

import com.munzenberger.money.core.model.EntryTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq

sealed class TransactionDetail {

    abstract val orderInTransaction: Long?

    data class Transfer(val transfer: com.munzenberger.money.core.Transfer) : TransactionDetail() {
        override val orderInTransaction = transfer.orderInTransaction
    }

    data class Entry(val entry: com.munzenberger.money.core.Entry) : TransactionDetail() {
        override val orderInTransaction = entry.orderInTransaction
    }
}

fun Transaction.getDetails(executor: QueryExecutor): List<TransactionDetail> {

    val details = mutableListOf<TransactionDetail>()

    val transfers = TransferTable.select()
            .where(TransferTable.transactionColumn.eq(identity))
            .build().let { executor.getList(it, TransferResultSetMapper()) }

    details += transfers.map { TransactionDetail.Transfer(it) }

    val entries = EntryTable.select()
            .where(EntryTable.transactionColumn.eq(identity))
            .build().let { executor.getList(it, EntryResultSetMapper()) }

    details += entries.map { TransactionDetail.Entry(it) }

    return details.sortedBy { it.orderInTransaction }
}
