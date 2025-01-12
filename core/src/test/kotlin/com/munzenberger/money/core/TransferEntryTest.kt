package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class TransferEntryTest : MoneyEntityTest<TransferEntryIdentity, TransferEntry>() {
    override fun createEntity() = TransferEntry().randomize()

    override fun getEntity(identity: TransferEntryIdentity) = TransferEntry.get(identity, database)

    override fun findEntities() = TransferEntry.find(database)

    override fun updateEntity(entity: TransferEntry) {
        entity.randomize()
    }

    override fun assertEntityPropertiesAreEquals(
        p1: TransferEntry,
        p2: TransferEntry,
    ) {
        assertEquals(p1.transactionRef.getIdentity(database)!!, p2.transactionRef.getIdentity(database)!!)
        assertEquals(p1.account?.identity, p2.account?.identity)
        assertEquals(p1.amount, p2.amount)
        assertEquals(p1.number, p2.number)
        assertEquals(p1.memo, p2.memo)
        assertEquals(p1.status, p2.status)
        assertEquals(p1.orderInTransaction, p2.orderInTransaction)
    }

    override fun createInvalidIdentity() = TransferEntryIdentity(42L)
}
