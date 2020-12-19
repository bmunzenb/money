package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountRegisterTest : MoneyDatabaseTestSupport() {

    @Test
    fun getAccountRegister() {

        val account1 = Account().apply {
            randomize()
            initialBalance = Money.valueOf(100)
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            initialBalance = Money.valueOf(-50)
            save(database)
        }

        val transaction = Transaction().apply {
            randomize()
            account = account1
            save(database)
        }

        // transfer 42 units from account2 to account1
        val transfer = Transfer().apply {
            randomize()
            account = account2
            amount = Money.valueOf(42)
            setTransaction(transaction)
            save(database)
        }

        val register1 = account1.getRegister(database)

        assertEquals(1, register1.size)

        val registerEntry1 = register1[0]

        assertEquals(transaction.identity, registerEntry1.transactionId)
        assertEquals(transaction.date, registerEntry1.date)
        assertEquals(transaction.payee!!.identity, registerEntry1.payeeId)
        assertEquals(transaction.payee!!.name, registerEntry1.payeeName)
        assertEquals(42, registerEntry1.amount.value)
        assertEquals(100 + 42, registerEntry1.balance.value)
        assertEquals(transaction.number, registerEntry1.number)
        assertEquals(transaction.memo, registerEntry1.memo)
        assertEquals(transaction.status, registerEntry1.status)
        assertEquals(1, registerEntry1.categories.size)

        val category1 = registerEntry1.categories[0]

        assertEquals(account2.identity, category1.accountId)
        assertEquals(account2.name, category1.accountName)

        val register2 = account2.getRegister(database)

        assertEquals(1, register2.size)

        val registerEntry2 = register2[0]

        assertEquals(transaction.identity, registerEntry2.transactionId)
        assertEquals(transaction.date, registerEntry2.date)
        assertEquals(transaction.payee!!.identity, registerEntry2.payeeId)
        assertEquals(transaction.payee!!.name, registerEntry2.payeeName)
        assertEquals(-42, registerEntry2.amount.value)
        assertEquals(-50 - 42, registerEntry2.balance.value)
        assertEquals(transfer.number, registerEntry2.number)
        assertEquals(transfer.memo, registerEntry2.memo)
        assertEquals(transfer.status, registerEntry2.status)
        assertEquals(1, registerEntry2.categories.size)

        val category2 = registerEntry2.categories[0]

        assertEquals(account1.identity, category2.accountId)
        assertEquals(account1.name, category2.accountName)
    }

    @Test
    fun `update status of parent transaction`() {

        val account1 = Account().apply {
            randomize()
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            save(database)
        }

        val transaction = Transaction().apply {
            randomize()
            account = account1
            status = TransactionStatus.UNRECONCILED
            save(database)
        }

        val transfer = Transfer().apply {
            randomize()
            account = account2
            status = TransactionStatus.UNRECONCILED
            setTransaction(transaction)
            save(database)
        }

        val register1 = account1.getRegister(database)

        register1[0].updateStatus(TransactionStatus.RECONCILED, database)

        val transaction1 = Transaction.get(transaction.identity!!, database)!!
        val transfer1 = Transfer.get(transfer.identity!!, database)!!
        assertEquals(TransactionStatus.RECONCILED, transaction1.status)
        assertEquals(TransactionStatus.UNRECONCILED, transfer1.status)
    }

    @Test
    fun `update status of child transfer`() {

        val account1 = Account().apply {
            randomize()
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            save(database)
        }

        val transaction = Transaction().apply {
            randomize()
            account = account1
            status = TransactionStatus.UNRECONCILED
            save(database)
        }

        val transfer = Transfer().apply {
            randomize()
            account = account2
            status = TransactionStatus.UNRECONCILED
            setTransaction(transaction)
            save(database)
        }

        val register2 = account2.getRegister(database)

        register2[0].updateStatus(TransactionStatus.RECONCILED, database)

        val transaction2 = Transaction.get(transaction.identity!!, database)!!
        val transfer2 = Transfer.get(transfer.identity!!, database)!!
        assertEquals(TransactionStatus.UNRECONCILED, transaction2.status)
        assertEquals(TransactionStatus.RECONCILED, transfer2.status)
    }
}
