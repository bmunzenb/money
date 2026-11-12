package com.munzenberger.money.core.transaction

import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionEntry

data class TransactionWithEntries(
    val transaction: Transaction,
    val entries: List<TransactionEntry>
)
