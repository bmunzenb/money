package com.munzenberger.money.data.api.bank

import com.munzenberger.money.data.api.EntityNotFoundException

interface BankWriter {
    fun add(bank: Bank)

    /** Updates the stored bank with [bank]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(bank: Bank)

    fun removeById(bankId: BankId)
}

fun BankWriter.remove(bank: Bank) {
    removeById(bank.id)
}
