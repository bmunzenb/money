package com.munzenberger.money.core

import org.junit.Assert
import org.junit.Test

class AccountBalanceTest : MoneyDatabaseTestSupport() {

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

            CategoryEntry().apply {
                setTransaction(it)
                category = Category().randomize()
                amount = Money.valueOf(76)
                save(database)
            }
        }

        val balance1 = account1.getBalance(database)
        Assert.assertEquals("Account1 balance", Money.valueOf(100L + 42L + 76L), balance1)

        val balance2 = account2.getBalance(database)
        Assert.assertEquals("Account2 balance", Money.valueOf(-100L - 42L), balance2)
    }
}