package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.Money

interface TransactionEntry {
    val transactionId: TransactionId
    val amount: Money
    val memo: String?
    val orderInTransaction: Int
}
