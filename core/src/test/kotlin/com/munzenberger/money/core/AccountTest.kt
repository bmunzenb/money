package com.munzenberger.money.core

import org.junit.Assert.*
import org.junit.Test

class AccountTest : PersistableTest<Account>() {

    override fun createPersistable() = Account().randomize()

    override fun getPersistable(identity: Long) = Account.get(identity, database)

    override fun getAllPersistables() = Account.getAll(database)

    override fun updatePersistable(persistable: Account) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Account, p2: Account) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.number, p2.number)
        assertEquals(p1.accountType?.identity, p2.accountType?.identity)
        assertEquals(p1.bank?.identity, p2.bank?.identity)
    }

    @Test
    fun `optional fields`() {

        val account = Account().apply {
            name = randomString()
            accountType = AccountType().randomize()
        }

        account.save(database)

        Account.get(account.identity!!, database).apply {
            assertNotNull(this)
            assertNull(this!!.bank)
            assertNull(this.number)
        }
    }
}
