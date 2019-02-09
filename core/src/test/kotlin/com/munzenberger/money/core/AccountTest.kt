package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class AccountTest : PersistableTest<Account>() {

    override fun createPersistable() = Account(database).apply {
        name = "Primary Savings"
        number = "112233445566"

        accountType = AccountType(database).apply {
            name = "Savings"
            category = AccountType.Category.ASSETS
        }

        bank = Bank(database).apply {
            name = "Standard Federal"
        }
    }

    override fun getPersistable(identity: Long) = Account.get(identity, database)

    override fun getAllPersistables() = Account.getAll(database)

    override fun updatePersistable(persistable: Account) {

        persistable.apply {
            name = "Shared Checking"
            number = "998877665544"

            accountType = AccountType(database).apply {
                name = "Checking"
                category = AccountType.Category.ASSETS
            }

            bank = Bank(database).apply {
                name = "Chemical Bank"
            }
        }
    }

    override fun assertPersistablePropertiesAreEquals(p1: Account, p2: Account) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.number, p2.number)
        assertEquals(p1.accountType?.identity, p2.accountType?.identity)
        assertEquals(p1.bank?.identity, p2.bank?.identity)
    }
}
