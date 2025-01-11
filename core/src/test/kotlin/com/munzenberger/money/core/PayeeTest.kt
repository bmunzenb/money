package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class PayeeTest : MoneyEntityTest<PayeeIdentity, Payee>() {

    override fun createPersistable() = Payee().randomize()

    override fun getPersistable(identity: PayeeIdentity) = Payee.get(identity, database)

    override fun getAllPersistables() = Payee.getAll(database)

    override fun updatePersistable(persistable: Payee) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Payee, p2: Payee) {
        assertEquals(p1.name, p2.name)
    }

    override fun createInvalidIdentity() = PayeeIdentity(42L)
}
