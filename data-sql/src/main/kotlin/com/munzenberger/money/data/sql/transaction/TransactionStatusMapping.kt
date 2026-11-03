package com.munzenberger.money.data.sql.transaction

import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId

/** Maps a `TransactionStatus` row, or the status columns joined into another row. */
internal fun transactionStatus(id: Long, value: String) = TransactionStatus(
    id = TransactionStatusId(id),
    value = TransactionStatusConstant.valueOf(value),
)
