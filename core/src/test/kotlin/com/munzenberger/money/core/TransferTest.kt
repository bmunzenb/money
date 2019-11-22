package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class TransferTest : PersistableTest<Transfer>() {

    override fun createPersistable() = Transfer().randomize()

    override fun getPersistable(identity: Long) = Transfer.observableGet(identity, database)

    override fun getAllPersistables() = Transfer.observableGetAll(database)

    override fun updatePersistable(persistable: Transfer) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Transfer, p2: Transfer) {
        assertEquals(p1.category?.identity, p2.category?.identity)
        assertEquals(p1.amount, p2.amount)
        assertEquals(p1.memo, p2.memo)
    }
}
