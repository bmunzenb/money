package com.munzenberger.money.core

import org.junit.Assert.*
import org.junit.Test
import java.util.*

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
            save(database)
        }

        Account.get(account.identity!!, database).apply {
            assertNotNull(this)
            assertNull(this!!.bank)
            assertNull(this.number)
        }
    }

    @Test
    fun balance() {

        val account1 = Account().apply {
            randomize()
            save(database)
        }

        val accountCategory = Category().apply {
            this.account = account1
            save(database)
        }

        val transaction1 = Transaction().apply {
            this.account = account1
            this.date = Date()
            save(database)
        }

        // Transfers -> Transaction -> Account = Amount added to that account
        Transfer().apply {
            this.setTransaction(transaction1)
            this.category = Category().apply { randomize() }
            this.amount = 42
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            save(database)
        }

        val transaction2 = Transaction().apply {
            this.account = account2
            this.date = Date()
            save(database)
        }

        // Transfers -> Category -> Account -> Amount subtracted from that account
       Transfer().apply {
            this.setTransaction(transaction2)
            this.category = accountCategory
            this.amount = 88
            save(database)
        }

        val balance1 = account1.balance(database)
        assertEquals("Account1 should have credit of 42 and debit of 88", Money.valueOf(42 - 88), balance1)

        val balance2 = account2.balance(database)
        assertEquals("Account2 should have credit of 88", Money.valueOf(88), balance2)
    }
}
