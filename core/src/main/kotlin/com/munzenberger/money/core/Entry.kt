package com.munzenberger.money.core

interface EntryIdentity : Identity

interface Entry<I : EntryIdentity> : MoneyEntity<I> {
    var amount: Money?

    var memo: String?

    var orderInTransaction: Int?

    fun setTransaction(transaction: Transaction)
}
