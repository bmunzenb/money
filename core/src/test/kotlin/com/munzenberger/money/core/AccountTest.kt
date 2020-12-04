package com.munzenberger.money.core

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

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
        assertEquals(p1.initialBalance, p2.initialBalance)
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
            initialBalance = Money.valueOf(10)
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            initialBalance = Money.valueOf(-10)
            save(database)
        }

        val account3 = Account().apply {
            randomize()
            initialBalance = null
            save(database)
        }

        val transaction1 = Transaction().apply {
            this.account = account1
            this.date = LocalDate.now()
            save(database)
        }

        // Transfer 42 units from account2 to account1
        // account1 -> 10 + 42 = 52
        // account2 -> -10 - 42 = -52
        Transfer().apply {
            this.setTransaction(transaction1)
            this.account = account2
            this.amount = Money.valueOf(42)
            save(database)
        }

        val transaction2 = Transaction().apply {
            this.account = account2
            this.date = LocalDate.now()
            save(database)
        }

        // Transfer 88 units from account3 to account2
        // account2 -> -52 + 88 = 36
        // account3 -> -88
       Transfer().apply {
            this.setTransaction(transaction2)
            this.account = account3
            this.amount = Money.valueOf(88)
            save(database)
        }

        // Transfer 10 units from account1 to account2
        // account1 -> 52 - 10 = 42
        // account3 -> 36 + 10 = 46
        Transfer().apply {
            this.setTransaction(transaction2)
            this.account = account1
            this.amount = Money.valueOf(10)
            save(database)
        }

        val balance1 = account1.getBalance(database)
        assertEquals(Money.valueOf(10 + 42 - 10), balance1)

        val balance2 = account2.getBalance(database)
        assertEquals(Money.valueOf(-10 - 42 + 88 + 10), balance2)

        val balance3 = account3.getBalance(database)
        assertEquals(Money.valueOf(-88), balance3)
    }
}
