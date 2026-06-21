package com.munzenberger.money.data.sql.transaction

import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.sql.createTestDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SqlTransactionStatusRepositoryTest {

    private fun createRepository(context: CoroutineDispatcher): SqlTransactionStatusRepository {
        return SqlTransactionStatusRepository(createTestDatabase(), context)
    }

    @Test
    fun `transactionStatuses emits every seeded variant exactly once`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val values = repository.transactionStatuses.first().map { it.value }
        assertEquals(TransactionStatusConstant.entries, values.sortedBy { it.ordinal })
    }
}
