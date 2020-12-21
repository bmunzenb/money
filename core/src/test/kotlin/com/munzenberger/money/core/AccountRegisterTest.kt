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
            orderInTransaction = 0
            setTransaction(transaction)
            save(database)
        }

        val category = Category().apply {
            randomize()
            save(database)
        }

        // spend 76 units on category
        Entry().apply {
            randomize()
            amount = Money.valueOf(-76)
            orderInTransaction = 1
            setCategory(category)
            setTransaction(transaction)
            save(database)
        }

        val register1 = account1.getRegister(database)

        val expectedRegister1 = listOf(
                RegisterEntry(
                        transactionId = transaction.identity!!,
                        date = transaction.date!!,
                        payeeId = transaction.payee!!.identity!!,
                        payeeName = transaction.payee!!.name!!,
                        amount = Money.valueOf(42 -76),
                        balance = Money.valueOf(100 +42 -76),
                        memo = transaction.memo!!,
                        number = transaction.number!!,
                        status = transaction.status!!,
                        details = listOf(
                                RegisterEntry.Detail.Transfer(
                                        transferId = transfer.identity!!,
                                        accountId = transfer.account!!.identity!!,
                                        accountName = transfer.account!!.name!!,
                                        orderInTransaction = transfer.orderInTransaction!!,
                                        isTransactionAccount = false
                                ),
                                RegisterEntry.Detail.Entry(
                                        categoryId = category.identity!!,
                                        categoryName = category.name!!,
                                        parentCategoryName = null,
                                        orderInTransaction = 1
                                )
                        ),
                        registerAccountId = account1.identity!!,
                        transactionAccountId = transaction.account!!.identity!!
                )
        )

        assertEquals(expectedRegister1, register1)

        val register2 = account2.getRegister(database)

        val expectedRegister2 = listOf(
                RegisterEntry(
                        transactionId = transaction.identity!!,
                        date = transaction.date!!,
                        payeeId = transaction.payee!!.identity!!,
                        payeeName = transaction.payee!!.name!!,
                        amount = Money.valueOf(-42),
                        balance = Money.valueOf(-50 -42),
                        memo = transfer.memo!!,
                        number = transfer.number!!,
                        status = transfer.status!!,
                        details = listOf(
                                RegisterEntry.Detail.Transfer(
                                        transferId = transfer.identity!!,
                                        accountId = transaction.account!!.identity!!,
                                        accountName = transaction.account!!.name!!,
                                        orderInTransaction = transfer.orderInTransaction!!,
                                        isTransactionAccount = true
                                )
                        ),
                        registerAccountId = account2.identity!!,
                        transactionAccountId = transaction.account!!.identity!!
                )
        )

        assertEquals(expectedRegister2, register2)
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
