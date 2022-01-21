package com.munzenberger.money.core

interface Entry : Persistable {

    var amount: Money?

    var memo: String?

    var orderInTransaction: Int?

    fun setTransaction(transaction: Transaction)
}
