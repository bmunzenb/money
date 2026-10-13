package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.EntityNotFoundException

interface TransactionWriter {
    fun add(transaction: Transaction)

    /** Updates the stored transaction with [transaction]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(transaction: Transaction)

    fun removeById(transactionId: TransactionId)
}

fun TransactionWriter.remove(transaction: Transaction) {
    removeById(transaction.id)
}
