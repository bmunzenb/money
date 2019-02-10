package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class BankTest : PersistableTest<Bank>() {

    override fun createPersistable() = Bank(database).randomize()

    override fun getPersistable(identity: Long) = Bank.get(identity, database)

    override fun getAllPersistables() = Bank.getAll(database)

    override fun updatePersistable(persistable: Bank) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Bank, p2: Bank) {
        assertEquals(p1.name, p2.name)
    }
}
