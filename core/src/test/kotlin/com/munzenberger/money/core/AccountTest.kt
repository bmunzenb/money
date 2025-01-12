package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class AccountTest : MoneyEntityTest<AccountIdentity, Account>() {
    override fun createEntity() = Account().randomize()

    override fun getEntity(identity: AccountIdentity) = Account.get(identity, database)

    override fun findEntities() = Account.find(database)

    override fun createInvalidIdentity() = AccountIdentity(42L)

    override fun updateEntity(entity: Account) {
        entity.randomize()
    }

    override fun assertEntityPropertiesAreEquals(
        p1: Account,
        p2: Account,
    ) {
        assertEquals(p1.name, p2.name)
        assertEquals(p1.number, p2.number)
        assertEquals(p1.accountType?.identity, p2.accountType?.identity)
        assertEquals(p1.bank?.identity, p2.bank?.identity)
        assertEquals(p1.initialBalance, p2.initialBalance)
    }

    @Test
    fun `optional fields`() {
        val account =
            Account().apply {
                name = Random.nextString()
                accountType = AccountType().randomize()
                save(database)
            }

        Account.get(account.identity!!, database).apply {
            assertNotNull(this)
            assertNull(this!!.bank)
            assertNull(this.number)
        }
    }
}
