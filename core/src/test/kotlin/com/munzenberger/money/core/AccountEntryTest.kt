package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountEntryTest : MoneyDatabaseTestSupport() {

    @Test
    fun `get account entries for transfer`() {

        val account1 = Account().apply {
            randomize()
            save(database)
        }

        val account2 = Account().apply {
            randomize()
            save(database)
        }

        val payee = Payee().apply {
            randomize()
            save(database)
        }

        val transaction = Transaction().apply {
            randomize()
            account = account1
            this.payee = payee
            save(database)
        }

        val transfer = TransferEntry().apply {
            randomize()
            setTransaction(transaction)
            account = account2
            save(database)
        }

        val entries1 = account1.getAccountEntries(database)

        val expected1 = listOf<AccountEntry>(
                AccountEntry.Transaction(
                        transactionId = transaction.identity!!,
                        date = transaction.date!!,
                        payeeId = payee.identity!!,
                        payeeName = payee.name!!,
                        amount = transfer.amount!!,
                        balance = account1.initialBalance!! + transfer.amount!!,
                        memo = transaction.memo,
                        number = transaction.number,
                        status = transaction.status!!,
                        details = listOf(
                                AccountEntry.Transaction.Detail.Transfer(
                                        transferId = transfer.identity!!,
                                        accountId = account2.identity!!,
                                        accountName = account2.name!!,
                                        orderInTransaction = transfer.orderInTransaction!!
                                )
                        )
                )
        )

        assertEquals(expected1, entries1)

        val entries2 = account2.getAccountEntries(database)

        val expected2 = listOf<AccountEntry>(
                AccountEntry.Transfer(
                        transferId = transfer.identity!!,
                        transactionId = transaction.identity!!,
                        date = transaction.date!!,
                        payeeId = payee.identity!!,
                        payeeName = payee.name!!,
                        amount = transfer.amount!!.negate(),
                        balance = account2.initialBalance!! - transfer.amount!!,
                        memo = transfer.memo,
                        number = transfer.number,
                        status = transfer.status!!,
                        transactionAccountId = account1.identity!!,
                        transactionAccountName = account1.name!!
                )
        )

        assertEquals(expected2, entries2)
    }

    @Test
    fun `get account entries for entry`() {

        val account = Account().apply {
            randomize()
            save(database)
        }

        val payee = Payee().apply {
            randomize()
            save(database)
        }

        val transaction = Transaction().apply {
            randomize()
            this.account = account
            this.payee = payee
            save(database)
        }

        val parentCategory = Category().apply {
            randomize()
            save(database)
        }

        val category = Category().apply {
            randomize()
            setParent(parentCategory)
            save(database)
        }

        val categoryEntry = CategoryEntry().apply {
            randomize()
            setTransaction(transaction)
            this.category = category
            save(database)
        }

        val entries = account.getAccountEntries(database)

        val expected = listOf<AccountEntry>(
                AccountEntry.Transaction(
                        transactionId = transaction.identity!!,
                        date = transaction.date!!,
                        payeeId = payee.identity!!,
                        payeeName = payee.name!!,
                        amount = categoryEntry.amount!!,
                        balance = account.initialBalance!! + categoryEntry.amount!!,
                        memo = transaction.memo,
                        number = transaction.number,
                        status = transaction.status!!,
                        details = listOf(
                                AccountEntry.Transaction.Detail.Entry(
                                        entryId = categoryEntry.identity!!,
                                        categoryId = category.identity!!,
                                        categoryName = category.name!!,
                                        parentCategoryName = parentCategory.name!!,
                                        orderInTransaction = categoryEntry.orderInTransaction!!
                                )
                        )
                )
        )

        assertEquals(expected, entries)
    }

    @Test
    fun `get account entries for orphaned transactions`() {

        val account = Account().apply {
            randomize()
            save(database)
        }

        val payee = Payee().apply {
            randomize()
            save(database)
        }

        val transaction = Transaction().apply {
            randomize()
            this.account = account
            this.payee = payee
            save(database)
        }

        val entries = account.getAccountEntries(database)

        val expected = listOf<AccountEntry>(
                AccountEntry.Transaction(
                        transactionId = transaction.identity!!,
                        date = transaction.date!!,
                        payeeId = payee.identity!!,
                        payeeName = payee.name!!,
                        amount = Money.ZERO,
                        balance = account.initialBalance!!,
                        memo = transaction.memo,
                        number = transaction.number,
                        status = transaction.status!!,
                        details = emptyList()
                )
        )

        assertEquals(expected, entries)
    }
}
