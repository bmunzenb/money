package com.munzenberger.money.data.sql.account

import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.sql.createTestDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SqlAccountTypeRepositoryTest {

    private fun createRepository(context: CoroutineDispatcher): SqlAccountTypeRepository {
        return SqlAccountTypeRepository(createTestDatabase(), context)
    }

    @Test
    fun `accountTypes emits every seeded variant exactly once`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val values = repository.accountTypes.first().map { it.value }
        assertEquals(AccountTypeConstant.entries, values.sortedBy { it.ordinal })
    }

    @Test
    fun `accountTypes joins each variant to its class`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val classesByType = repository.accountTypes.first()
            .associate { it.value to it.accountClass.value }

        assertEquals(
            mapOf(
                AccountTypeConstant.Savings to AccountClassConstant.Assets,
                AccountTypeConstant.Checking to AccountClassConstant.Assets,
                AccountTypeConstant.Asset to AccountClassConstant.Assets,
                AccountTypeConstant.Cash to AccountClassConstant.Assets,
                AccountTypeConstant.Credit to AccountClassConstant.Liabilities,
                AccountTypeConstant.Loan to AccountClassConstant.Liabilities,
            ),
            classesByType,
        )
    }
}
