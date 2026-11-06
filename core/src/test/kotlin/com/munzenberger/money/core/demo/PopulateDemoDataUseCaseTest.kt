package com.munzenberger.money.core.demo

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyWriter
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.bank.Bank
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PopulateDemoDataUseCaseTest {

    private val accountTypes = AccountTypeConstant.entries.mapIndexed { index, value ->
        val accountClass = when (value) {
            AccountTypeConstant.Credit, AccountTypeConstant.Loan ->
                AccountClass(id = AccountClassId(2), value = AccountClassConstant.Liabilities)
            else -> AccountClass(id = AccountClassId(1), value = AccountClassConstant.Assets)
        }
        AccountType(id = AccountTypeId(index + 1L), accountClass = accountClass, value = value)
    }

    private val categoryTypes = CategoryTypeConstant.entries.mapIndexed { index, value ->
        CategoryType(id = CategoryTypeId(index + 1L), value = value)
    }

    private val transactionStatuses = TransactionStatusConstant.entries.mapIndexed { index, value ->
        TransactionStatus(id = TransactionStatusId(index + 1L), value = value)
    }

    private val banks = mutableListOf<Bank>()
    private val accounts = mutableListOf<Account>()
    private val categories = mutableListOf<Category>()
    private val payees = mutableListOf<Payee>()
    private val transactions = mutableListOf<Transaction>()
    private val categoryEntries = mutableListOf<CategoryEntry>()
    private val transferEntries = mutableListOf<TransferEntry>()
    private val writer = mockk<MoneyWriter> {
        every { add(capture(banks)) } returns Unit
        every { add(capture(accounts)) } returns Unit
        every { add(capture(categories)) } returns Unit
        every { add(capture(payees)) } returns Unit
        every { add(capture(transactions)) } returns Unit
        every { add(capture(categoryEntries)) } returns Unit
        every { add(capture(transferEntries)) } returns Unit
    }
    private val repository = mockk<MoneyRepository> {
        every { this@mockk.accountTypes } returns flowOf(this@PopulateDemoDataUseCaseTest.accountTypes)
        every { this@mockk.categoryTypes } returns flowOf(this@PopulateDemoDataUseCaseTest.categoryTypes)
        every { this@mockk.transactionStatuses } returns flowOf(this@PopulateDemoDataUseCaseTest.transactionStatuses)
        coEvery { transaction<Unit>(any()) } answers { firstArg<MoneyWriter.() -> Unit>().invoke(writer) }
    }

    private val populateDemoData = PopulateDemoDataUseCase()

    @Test
    fun `adds several banks each with a few accounts`() = runTest {
        val result = populateDemoData(repository)

        assertEquals(PopulateDemoDataResult.Success, result)
        assertTrue(banks.size > 1)
        banks.forEach { bank ->
            assertTrue(accounts.count { it.bankId == bank.id } > 1, "${bank.name} has too few accounts")
        }
    }

    @Test
    fun `every account belongs to an added bank`() = runTest {
        populateDemoData(repository)

        val bankIds = banks.map { it.id }.toSet()
        assertTrue(accounts.all { it.bankId in bankIds })
    }

    @Test
    fun `accounts use varying types from the repository`() = runTest {
        populateDemoData(repository)

        assertTrue(accounts.all { it.accountType in accountTypes })
        assertTrue(accounts.map { it.accountType }.distinct().size > 1)
    }

    @Test
    fun `adds each bank before its accounts`() = runTest {
        populateDemoData(repository)

        verifyOrder {
            banks.forEach { bank ->
                writer.add(bank)
                accounts.filter { it.bankId == bank.id }.forEach { writer.add(it) }
            }
        }
    }

    @Test
    fun `adds income and expense categories, some with subcategories`() = runTest {
        populateDemoData(repository)

        assertEquals(categoryTypes.toSet(), categories.map { it.type }.toSet())
        val subcategories = categories.filter { it.parentId != null }
        assertTrue(subcategories.isNotEmpty())
        subcategories.forEach { subcategory ->
            val parentIndex = categories.indexOfFirst { it.id == subcategory.parentId }
            assertTrue(parentIndex in 0..<categories.indexOf(subcategory), "${subcategory.name} has no earlier parent")
            assertEquals(categories[parentIndex].type, subcategory.type)
        }
    }

    @Test
    fun `adds several transactions to every account`() = runTest {
        populateDemoData(repository)

        accounts.forEach { account ->
            assertTrue(
                transactions.count { it.accountId == account.id } > 1,
                "${account.name} has too few transactions",
            )
        }
    }

    @Test
    fun `transactions refer to added payees and statuses`() = runTest {
        populateDemoData(repository)

        val payeeIds = payees.map { it.id }.toSet()
        assertTrue(transactions.mapNotNull { it.payeeId }.all { it in payeeIds })
        assertTrue(transactions.all { it.status in transactionStatuses })
        assertTrue(payees.all { payee -> transactions.any { it.payeeId == payee.id } }, "Some payees are unused")
    }

    @Test
    fun `entries refer to added transactions, categories, and other accounts`() = runTest {
        populateDemoData(repository)

        val transactionsById = transactions.associateBy { it.id }
        val categoryIds = categories.map { it.id }.toSet()
        val accountIds = accounts.map { it.id }.toSet()
        assertTrue(categoryEntries.all { it.transactionId in transactionsById && it.categoryId in categoryIds })
        transferEntries.forEach { entry ->
            val transaction = assertNotNull(transactionsById[entry.transactionId])
            assertTrue(entry.accountId in accountIds)
            assertNotEquals(transaction.accountId, entry.accountId)
            assertTrue(entry.status in transactionStatuses)
        }
    }

    @Test
    fun `some transactions have a single entry and others are split`() = runTest {
        populateDemoData(repository)

        val entriesByTransaction = transactions.associate { transaction ->
            transaction.id to categoryEntries.filter { it.transactionId == transaction.id } +
                transferEntries.filter { it.transactionId == transaction.id }
        }
        assertTrue(entriesByTransaction.values.all { it.isNotEmpty() })
        val single = entriesByTransaction.values.filter { it.size == 1 }
        val split = entriesByTransaction.values.filter { it.size > 1 }
        assertTrue(single.any { it.single() is CategoryEntry })
        assertTrue(single.any { it.single() is TransferEntry })
        assertTrue(split.any { entries -> entries.count { it is CategoryEntry } > 1 })
        assertTrue(split.any { entries -> entries.any { it is CategoryEntry } && entries.any { it is TransferEntry } })
        split.forEach { entries ->
            assertEquals(entries.indices.toList(), entries.map { it.orderInTransaction }.sorted())
        }
    }

    @Test
    fun `adds each transaction after its account and before its entries`() = runTest {
        populateDemoData(repository)

        verifyOrder {
            accounts.forEach { writer.add(it) }
            transactions.forEach { transaction ->
                writer.add(transaction)
                categoryEntries.filter { it.transactionId == transaction.id }.forEach { writer.add(it) }
                transferEntries.filter { it.transactionId == transaction.id }.forEach { writer.add(it) }
            }
        }
    }

    @Test
    fun `returns failure when the write fails`() = runTest {
        val error = IllegalStateException("write failed")
        coEvery { repository.transaction<Unit>(any()) } throws error

        val result = populateDemoData(repository)

        assertEquals(PopulateDemoDataResult.Failure(error), result)
    }

    @Test
    fun `returns failure when an account type is missing`() = runTest {
        every { repository.accountTypes } returns flowOf(emptyList())

        val result = populateDemoData(repository)

        assertIs<PopulateDemoDataResult.Failure>(result)
    }

    @Test
    fun `returns failure when a category type is missing`() = runTest {
        every { repository.categoryTypes } returns flowOf(emptyList())

        val result = populateDemoData(repository)

        assertIs<PopulateDemoDataResult.Failure>(result)
    }

    @Test
    fun `returns failure when a transaction status is missing`() = runTest {
        every { repository.transactionStatuses } returns flowOf(emptyList())

        val result = populateDemoData(repository)

        assertIs<PopulateDemoDataResult.Failure>(result)
    }
}
