package com.munzenberger.money.data.api.payee

import com.munzenberger.money.data.api.EntityNotFoundException

interface PayeeWriter {
    fun add(payee: Payee)

    /** Updates the stored payee with [payee]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(payee: Payee)

    fun removeById(payeeId: PayeeId)
}

fun PayeeWriter.remove(payee: Payee) {
    removeById(payee.id)
}
