package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class TransactionTest : MoneyEntityTest<TransactionIdentity, Transaction>() {
    override fun createEntity() = Transaction().randomize()

    override fun getEntity(identity: TransactionIdentity) = Transaction.get(identity, database)

    override fun findEntities() = Transaction.find(database)

    override fun updateEntity(entity: Transaction) {
        entity.randomize()
    }

    override fun assertEntityPropertiesAreEquals(
        p1: Transaction,
        p2: Transaction,
    ) {
        assertEquals(p1.account?.identity, p2.account?.identity)
        assertEquals(p1.payee?.identity, p2.payee?.identity)
        assertEquals(p1.date, p2.date)
        assertEquals(p1.memo, p2.memo)
        assertEquals(p1.number, p2.number)
        assertEquals(p1.status, p2.status)
    }

    override fun createInvalidIdentity() = TransactionIdentity(42L)
}
