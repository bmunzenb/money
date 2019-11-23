package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class AccountTypeTest : PersistableTest<AccountType>() {

    override fun createPersistable() = AccountType().randomize()

    override fun getPersistable(identity: Long) = AccountType.get(identity, database)

    override fun getAllPersistables() = AccountType.getAll(database)

    override fun updatePersistable(persistable: AccountType) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: AccountType, p2: AccountType) {
        assertEquals(p1.category, p2.category)
        assertEquals(p1.variant, p2.variant)
    }

    override fun `can store and retrieve a list of persistables`() {

        // verify that the initial values are present

        getAllPersistables().apply {
            assertTrue(isNotEmpty())
        }
    }
}
