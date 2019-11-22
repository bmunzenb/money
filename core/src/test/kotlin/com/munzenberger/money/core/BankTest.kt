package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class BankTest : PersistableTest<Bank>() {

    override fun createPersistable() = Bank().randomize()

    override fun getPersistable(identity: Long) = Bank.observableGet(identity, database)

    override fun getAllPersistables() = Bank.observableGetAll(database)

    override fun updatePersistable(persistable: Bank) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Bank, p2: Bank) {
        assertEquals(p1.name, p2.name)
    }
}
