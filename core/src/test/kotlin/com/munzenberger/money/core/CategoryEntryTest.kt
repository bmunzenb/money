package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class CategoryEntryTest : MoneyEntityTest<CategoryEntryIdentity, CategoryEntry>() {

    override fun createPersistable() =
            CategoryEntry().randomize()

    override fun getPersistable(identity: CategoryEntryIdentity) =
            CategoryEntry.get(identity, database)

    override fun getAllPersistables() =
            CategoryEntry.getAll(database)

    override fun updatePersistable(persistable: CategoryEntry) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: CategoryEntry, p2: CategoryEntry) {
        assertEquals(p1.transactionRef.getIdentity(database)!!, p2.transactionRef.getIdentity(database)!!)
        assertEquals(p1.category!!.identity!!, p2.category!!.identity!!)
        assertEquals(p1.amount, p2.amount)
        assertEquals(p1.memo, p2.memo)
        assertEquals(p1.orderInTransaction, p2.orderInTransaction)
    }

    override fun createInvalidIdentity() = CategoryEntryIdentity(42L)
}
