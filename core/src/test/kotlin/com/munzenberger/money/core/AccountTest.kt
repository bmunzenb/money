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
    fun getBalance() {

        val account1 = Account().apply {
            randomize()
            initialBalance = Money.valueOf(100)
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            initialBalance = Money.valueOf(-100)
            save(database)
        }

        val transaction1 = Transaction().apply {
            randomize()
            account = account1
            save(database)
        }

        transaction1.let {

            Transfer().apply {
                setTransaction(it)
                account = account2
                amount = Money.valueOf(42)
                save(database)
            }

            Entry().apply {
                setTransaction(it)
                category = Category().randomize()
                amount = Money.valueOf(76)
                save(database)
            }
        }

        val balance1 = account1.getBalance(database)
        assertEquals("Account1 balance", Money.valueOf(100 +42 +76), balance1)

        val balance2 = account2.getBalance(database)
        assertEquals("Account2 balance", Money.valueOf(-100 -42), balance2)
    }
}
