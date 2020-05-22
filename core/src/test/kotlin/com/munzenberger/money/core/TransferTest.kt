package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class TransferTest : PersistableTest<Transfer>() {

    override fun createPersistable() = Transfer().randomize()

    override fun getPersistable(identity: Long) = Transfer.get(identity, database)

    override fun getAllPersistables() = Transfer.getAll(database)

    override fun updatePersistable(persistable: Transfer) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Transfer, p2: Transfer) {
        assertEquals(p1.category?.identity, p2.category?.identity)
        assertEquals(p1.amount, p2.amount)
        assertEquals(p1.memo, p2.memo)
        assertEquals(p1.number, p2.number)
        assertEquals(p1.status, p2.status)
    }
}
