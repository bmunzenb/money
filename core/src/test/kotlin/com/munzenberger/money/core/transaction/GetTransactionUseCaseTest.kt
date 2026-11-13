package com.munzenberger.money.core.transaction

import app.cash.turbine.test
import com.munzenberger.money.data.api.EntityNotFoundException
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.Transaction
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetTransactionUseCaseTest {

    private val unreconciled = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled)

    private val transaction = Transaction(accountId = AccountId(), status = unreconciled)

    private val transactionId = transaction.id

    private val groceries = categoryEntry(Money(-40), orderInTransaction = 0)
    private val toSavings = transferEntry(Money(-100), orderInTransaction = 1)
    private val household = categoryEntry(Money(-15), orderInTransaction = 2)

    private val transactionFlow = MutableStateFlow<Transaction?>(transaction)
    private val categoryEntriesFlow = MutableStateFlow(listOf(groceries, household))
    private val transferEntriesFlow = MutableStateFlow(listOf(toSavings))

    private val repository = mockk<MoneyRepository> {
        every { transactionById(transactionId) } returns transactionFlow
        every { categoryEntriesByTransactionId(transactionId) } returns categoryEntriesFlow
        every { transferEntriesByTransactionId(transactionId) } returns transferEntriesFlow
    }

    private val getTransaction = GetTransactionUseCase()

    @Test
    fun `emits the transaction with its category and transfer entries in order`() = runTest {
        getTransaction(repository, transactionId).test {
            assertEquals(
                Result.success(TransactionWithEntries(transaction, listOf(groceries, toSavings, household))),
                awaitItem(),
            )
        }
    }

    @Test
    fun `emits the transaction with no entries when it has none`() = runTest {
        categoryEntriesFlow.value = emptyList()
        transferEntriesFlow.value = emptyList()

        getTransaction(repository, transactionId).test {
            assertEquals(Result.success(TransactionWithEntries(transaction, emptyList())), awaitItem())
        }
    }

    @Test
    fun `emits the transaction again when it changes`() = runTest {
        getTransaction(repository, transactionId).test {
            awaitItem()

            val withMemo = transaction.copy(memo = "Weekly shop")
            transactionFlow.value = withMemo

            assertEquals(withMemo, awaitItem().getOrThrow().transaction)
        }
    }

    @Test
    fun `emits the entries again when a category entry changes`() = runTest {
        getTransaction(repository, transactionId).test {
            awaitItem()

            categoryEntriesFlow.value = listOf(groceries)

            assertEquals(listOf(groceries, toSavings), awaitItem().getOrThrow().entries)
        }
    }

    @Test
    fun `emits the entries again when a transfer entry changes`() = runTest {
        getTransaction(repository, transactionId).test {
            awaitItem()

            val toChecking = transferEntry(Money(-25), orderInTransaction = 3)
            transferEntriesFlow.value = listOf(toSavings, toChecking)

            assertEquals(listOf(groceries, toSavings, household, toChecking), awaitItem().getOrThrow().entries)
        }
    }

    @Test
    fun `emits a not-found failure when there's no such transaction`() = runTest {
        transactionFlow.value = null

        getTransaction(repository, transactionId).test {
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())
        }
    }

    @Test
    fun `keeps emitting after the transaction isn't found`() = runTest {
        getTransaction(repository, transactionId).test {
            awaitItem()

            transactionFlow.value = null
            assertIs<EntityNotFoundException>(awaitItem().exceptionOrNull())

            // The flow is still collecting, so a later value still arrives.
            transactionFlow.value = transaction
            assertEquals(transaction, awaitItem().getOrThrow().transaction)
        }
    }

    @Test
    fun `emits failure when the transaction flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.transactionById(transactionId) } returns flow { throw error }

        getTransaction(repository, transactionId).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    @Test
    fun `emits failure when the category entries flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.categoryEntriesByTransactionId(transactionId) } returns flow { throw error }

        getTransaction(repository, transactionId).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    @Test
    fun `emits failure when the transfer entries flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.transferEntriesByTransactionId(transactionId) } returns flow { throw error }

        getTransaction(repository, transactionId).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }

    @Test
    fun `a transaction's amount is the sum of its entries`() {
        val withEntries = TransactionWithEntries(transaction, listOf(groceries, toSavings, household))

        assertEquals(Money(-155), withEntries.amount)
    }

    @Test
    fun `a transaction with no entries has an amount of zero`() {
        assertEquals(Money(0), TransactionWithEntries(transaction, emptyList()).amount)
    }

    private fun categoryEntry(amount: Money, orderInTransaction: Int) = CategoryEntry(
        transactionId = transactionId,
        categoryId = CategoryId(),
        amount = amount,
        orderInTransaction = orderInTransaction,
    )

    private fun transferEntry(amount: Money, orderInTransaction: Int) = TransferEntry(
        transactionId = transactionId,
        accountId = AccountId(),
        amount = amount,
        status = unreconciled,
        orderInTransaction = orderInTransaction,
    )
}
