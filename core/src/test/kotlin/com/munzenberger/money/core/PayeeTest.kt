package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class PayeeTest : MoneyEntityTest<PayeeIdentity, Payee>() {
    override fun createEntity() = Payee().randomize()

    override fun getEntity(identity: PayeeIdentity) = Payee.get(identity, database)

    override fun findEntities() = Payee.find(database)

    override fun updateEntity(entity: Payee) {
        entity.randomize()
    }

    override fun assertEntityPropertiesAreEquals(
        p1: Payee,
        p2: Payee,
    ) {
        assertEquals(p1.name, p2.name)
    }

    override fun createInvalidIdentity() = PayeeIdentity(42L)
}
