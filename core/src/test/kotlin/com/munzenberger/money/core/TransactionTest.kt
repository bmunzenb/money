package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class TransactionTest : PersistableTest<Transaction>() {

    override fun createPersistable() = Transaction().randomize()

    override fun getPersistable(identity: Long) = Transaction.observableGet(identity, database)

    override fun getAllPersistables() = Transaction.observableGetAll(database)

    override fun updatePersistable(persistable: Transaction) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Transaction, p2: Transaction) {
        assertEquals(p1.account?.identity, p2.account?.identity)
        assertEquals(p1.payee?.identity, p2.payee?.identity)
        assertEquals(p1.date, p2.date)
        assertEquals(p1.memo, p2.memo)
    }
}
