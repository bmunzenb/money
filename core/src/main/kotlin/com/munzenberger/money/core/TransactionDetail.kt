package com.munzenberger.money.core

import com.munzenberger.money.core.model.CategoryEntryTable
import com.munzenberger.money.core.model.TransferTable
import com.munzenberger.money.sql.QueryExecutor
import com.munzenberger.money.sql.eq

// TODO move this down into the persistable instead of wrapping in another object
sealed class TransactionDetail {

    abstract val orderInTransaction: Int?
    abstract val amount: Money?

    data class Transfer(val transfer: com.munzenberger.money.core.Transfer) : TransactionDetail() {
        override val orderInTransaction = transfer.orderInTransaction
        override val amount = transfer.amount
    }

    data class Entry(val categoryEntry: com.munzenberger.money.core.CategoryEntry) : TransactionDetail() {
        override val orderInTransaction = categoryEntry.orderInTransaction
        override val amount = categoryEntry.amount
    }
}

fun Transaction.getDetails(executor: QueryExecutor): List<TransactionDetail> {

    val details = mutableListOf<TransactionDetail>()

    val transfers = TransferTable.select()
            .where(TransferTable.transactionColumn.eq(identity))
            .build().let { executor.getList(it, TransferResultSetMapper()) }

    details += transfers.map { TransactionDetail.Transfer(it) }

    val entries = CategoryEntryTable.select()
            .where(CategoryEntryTable.transactionColumn.eq(identity))
            .build().let { executor.getList(it, CategoryEntryResultSetMapper()) }

    details += entries.map { TransactionDetail.Entry(it) }

    return details.sortedBy { it.orderInTransaction }
}
