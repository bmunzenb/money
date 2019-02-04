package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class AccountTypeTest : PersistableTest<AccountType>() {

    override fun createPersistable() = AccountType(database).apply {
        name = "Savings"
        category = AccountType.Category.ASSETS
    }

    override fun getPersistable(identity: Long) = AccountType.get(identity, database)

    override fun getAllPersistables() = AccountType.getAll(database)

    override fun updatePersistable(persistable: AccountType) {
        persistable.name = "Credit Card"
        persistable.category = AccountType.Category.LIABILITIES
    }

    override fun assertPersistablePropertiesAreEquals(p1: AccountType, p2: AccountType) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.category, p2.category)
    }
}
