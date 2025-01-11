package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class BankTest : MoneyEntityTest<BankIdentity, Bank>() {

    override fun createPersistable() = Bank().randomize()

    override fun getPersistable(identity: BankIdentity) = Bank.get(identity, database)

    override fun getAllPersistables() = Bank.getAll(database)

    override fun createInvalidIdentity() = BankIdentity(42L)

    override fun updatePersistable(persistable: Bank) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Bank, p2: Bank) {
        assertEquals(p1.name, p2.name)
    }
}
