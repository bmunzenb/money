package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDetailTest : MoneyDatabaseTestSupport() {

    @Test
    fun `query for transaction details returns all types`() {

        val transaction = Transaction().apply {
            randomize()
            save(database)
        }

        val transfer1 = Transfer().apply {
            randomize()
            setTransaction(transaction)
            orderInTransaction = 3
            save(database)
        }

        val transfer2 = Transfer().apply {
            randomize()
            setTransaction(transaction)
            orderInTransaction = 0
            save(database)
        }

        val entry1 = Entry().apply {
            randomize()
            setTransaction(transaction)
            orderInTransaction = 2
            save(database)
        }

        val entry2 = Entry().apply {
            randomize()
            setTransaction(transaction)
            orderInTransaction = 1
            save(database)
        }

        val details = transaction.getDetails(database)

        assertEquals(4, details.size)

        details[0].let { assertTrue(it is TransactionDetail.Transfer && it.transfer.identity!! == transfer2.identity!!) }
        details[1].let { assertTrue(it is TransactionDetail.Entry && it.entry.identity!! == entry2.identity!!) }
        details[2].let { assertTrue(it is TransactionDetail.Entry && it.entry.identity!! == entry1.identity!!) }
        details[3].let { assertTrue(it is TransactionDetail.Transfer && it.transfer.identity!! == transfer1.identity!!) }
    }
}
