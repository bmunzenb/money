package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class EntryTest : PersistableTest<Entry>() {

    override fun createPersistable() =
            Entry().randomize()

    override fun getPersistable(identity: Long) =
            Entry.get(identity, database)

    override fun getAllPersistables() =
            Entry.getAll(database)

    override fun updatePersistable(persistable: Entry) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Entry, p2: Entry) {
        assertEquals(p1.transactionRef.getIdentity(database)!!, p2.transactionRef.getIdentity(database)!!)
        assertEquals(p1.categoryRef.getIdentity(database)!!, p2.categoryRef.getIdentity(database)!!)
        assertEquals(p1.amount, p2.amount)
        assertEquals(p1.memo, p2.memo)
        assertEquals(p1.orderInTransaction, p2.orderInTransaction)
    }
}