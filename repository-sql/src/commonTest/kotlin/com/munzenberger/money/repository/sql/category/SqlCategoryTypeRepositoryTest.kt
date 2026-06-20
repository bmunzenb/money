package com.munzenberger.money.repository.sql.category

import com.munzenberger.money.repository.api.category.CategoryTypeConstant
import com.munzenberger.money.repository.sql.createTestDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SqlCategoryTypeRepositoryTest {

    private fun createRepository(context: CoroutineDispatcher): SqlCategoryTypeRepository {
        return SqlCategoryTypeRepository(createTestDatabase(), context)
    }

    @Test
    fun `categoryTypes emits every seeded variant exactly once`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val values = repository.categoryTypes.first().map { it.value }
        assertEquals(CategoryTypeConstant.entries, values.sortedBy { it.ordinal })
    }
}
