package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class BankTest : MoneyEntityTest<BankIdentity, Bank>() {
    override fun createEntity() = Bank().randomize()

    override fun getEntity(identity: BankIdentity) = Bank.get(identity, database)

    override fun findEntities() = Bank.find(database)

    override fun createInvalidIdentity() = BankIdentity(42L)

    override fun updateEntity(entity: Bank) {
        entity.randomize()
    }

    override fun assertEntityPropertiesAreEquals(
        p1: Bank,
        p2: Bank,
    ) {
        assertEquals(p1.name, p2.name)
    }
}
