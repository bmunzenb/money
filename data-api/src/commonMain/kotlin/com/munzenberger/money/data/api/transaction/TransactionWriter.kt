package com.munzenberger.money.data.api.transaction

interface TransactionWriter {
    fun add(transaction: Transaction)

    fun update(transaction: Transaction)

    fun removeById(transactionId: TransactionId)
}

fun TransactionWriter.remove(transaction: Transaction) {
    removeById(transaction.id)
}
