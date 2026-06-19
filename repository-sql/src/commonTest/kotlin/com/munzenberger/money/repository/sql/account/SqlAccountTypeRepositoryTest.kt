package com.munzenberger.money.repository.sql.account

import com.munzenberger.money.repository.api.account.AccountTypeConstant
import com.munzenberger.money.repository.api.account.AccountTypeGroupConstant
import com.munzenberger.money.repository.sql.createTestDatabase
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
    fun `accountTypes joins each variant to its group`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val groupsByType = repository.accountTypes.first()
            .associate { it.value to it.group.value }

        assertEquals(
            mapOf(
                AccountTypeConstant.Savings to AccountTypeGroupConstant.Assets,
                AccountTypeConstant.Checking to AccountTypeGroupConstant.Assets,
                AccountTypeConstant.Asset to AccountTypeGroupConstant.Assets,
                AccountTypeConstant.Cash to AccountTypeGroupConstant.Assets,
                AccountTypeConstant.Credit to AccountTypeGroupConstant.Liabilities,
                AccountTypeConstant.Loan to AccountTypeGroupConstant.Liabilities,
            ),
            groupsByType,
        )
    }
}
