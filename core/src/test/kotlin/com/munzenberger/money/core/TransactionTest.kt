package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import java.util.*

class TransactionTest : PersistableTest<Transaction>() {

    override fun createPersistable() = Transaction(database).apply {

        account = Account(database).apply {
            name = "Standard Federal"
            accountType = AccountType(database).apply {
                name = "Savings"
                category = AccountType.Category.ASSETS
            }
        }

        payee = Payee(database).apply {
            name = "Gas Company"
        }

        date = Date()

        memo = "Lorem ipsum dolor sit amet."
    }

    override fun getPersistable(identity: Long) = Transaction.get(identity, database)

    override fun getAllPersistables() = Transaction.getAll(database)

    override fun updatePersistable(persistable: Transaction) {

        persistable.apply {

            account = Account(database).apply {
                name = "Chemical Bank"
                accountType = AccountType(database).apply {
                    name = "Checking"
                    category = AccountType.Category.ASSETS
                }
            }

            payee = Payee(database).apply {
                name = "Electric Company"
            }

            date = Date()

            memo = "Consectetur adipiscing elit"
        }
    }

    override fun assertPersistablePropertiesAreEquals(p1: Transaction, p2: Transaction) {
        assertEquals(p1.account?.identity, p2.account?.identity)
        assertEquals(p1.payee?.identity, p2.payee?.identity)
        assertEquals(p1.date, p2.date)
        assertEquals(p1.memo, p2.memo)
    }
}
