package com.munzenberger.money.data.api.bank

interface BankWriter {
    fun add(bank: Bank)

    fun update(bank: Bank)

    fun removeById(bankId: BankId)
}

fun BankWriter.remove(bank: Bank) {
    removeById(bank.id)
}
