package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class AccountTypeTest : PersistableTest<AccountType>() {

    override fun createPersistable() = AccountType().randomize()

    override fun getPersistable(identity: Long) = AccountType.get(identity, database)

    override fun getAllPersistables() = AccountType.getAll(database)

    override fun updatePersistable(persistable: AccountType) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: AccountType, p2: AccountType) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.category, p2.category)
    }

    override fun `can store and retrieve a list of persistables`() {

        // verify that the initial values are present

        getAllPersistables().test().assertComplete().apply {
            assertValue { it.isNotEmpty() }
        }
    }
}
