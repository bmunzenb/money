package com.munzenberger.money.data.api.payee

interface PayeeWriter {
    fun add(payee: Payee)

    fun update(payee: Payee)

    fun removeById(payeeId: PayeeId)
}

fun PayeeWriter.remove(payee: Payee) {
    removeById(payee.id)
}
