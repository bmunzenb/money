package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDetailTest : MoneyDatabaseTestSupport() {
    @Test
    fun `query for transaction details returns all types`() {
        val transaction =
            Transaction().apply {
                randomize()
                save(database)
            }

        val transfer1 =
            TransferEntry().apply {
                randomize()
                setTransaction(transaction)
                orderInTransaction = 3
                save(database)
            }

        val transfer2 =
            TransferEntry().apply {
                randomize()
                setTransaction(transaction)
                orderInTransaction = 0
                save(database)
            }

        val categoryEntry1 =
            CategoryEntry().apply {
                randomize()
                setTransaction(transaction)
                orderInTransaction = 2
                save(database)
            }

        val categoryEntry2 =
            CategoryEntry().apply {
                randomize()
                setTransaction(transaction)
                orderInTransaction = 1
                save(database)
            }

        val entries = transaction.getEntries(database)

        assertEquals(4, entries.size)

        entries[0].let { assertTrue(it is TransferEntry && it.identity!! == transfer2.identity!!) }
        entries[1].let { assertTrue(it is CategoryEntry && it.identity!! == categoryEntry2.identity!!) }
        entries[2].let { assertTrue(it is CategoryEntry && it.identity!! == categoryEntry1.identity!!) }
        entries[3].let { assertTrue(it is TransferEntry && it.identity!! == transfer1.identity!!) }
    }
}
