package com.munzenberger.money.repository.sql.category

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.munzenberger.money.repository.api.category.Category
import com.munzenberger.money.repository.api.category.CategoryId
import com.munzenberger.money.repository.api.category.CategoryType
import com.munzenberger.money.repository.api.category.CategoryTypeConstant
import com.munzenberger.money.repository.api.category.CategoryTypeId
import com.munzenberger.money.repository.api.category.remove
import com.munzenberger.money.repository.sql.MoneyDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class SqlCategoryRepositoryTest {

    private val income = CategoryType(id = CategoryTypeId(1), value = CategoryTypeConstant.Income)
    private val expense = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense)

    private fun createRepository(context: CoroutineDispatcher): SqlCategoryRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MoneyDatabase.Schema.create(driver)
        return SqlCategoryRepository(driver, context)
    }

    @Test
    fun `categories emits empty list when no categories exist`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        assertTrue(repository.categories.first().isEmpty())
    }

    @Test
    fun `add inserts a category`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category = Category(name = "Groceries", type = expense, memo = null)
        repository.add(category)
        assertEquals(listOf(category), repository.categories.first())
    }

    @Test
    fun `add inserts a category with a memo`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category = Category(name = "Groceries", type = expense, memo = "A memo")
        repository.add(category)
        assertEquals(listOf(category), repository.categories.first())
    }

    @Test
    fun `add inserts a category with an income type`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category = Category(name = "Salary", type = income, memo = null)
        repository.add(category)
        assertEquals(listOf(category), repository.categories.first())
    }

    @Test
    fun `add inserts a category with a parent`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val parent = Category(name = "Food", type = expense, memo = null)
        repository.add(parent)
        val child = Category(name = "Groceries", parent = parent.id, type = expense, memo = null)
        repository.add(child)
        val categories = repository.categories.first()
        assertContains(categories, parent)
        assertContains(categories, child)
    }

    @Test
    fun `add inserts multiple categories`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category1 = Category(name = "First Category", type = income, memo = null)
        val category2 = Category(name = "Second Category", type = expense, memo = "memo")
        repository.add(category1)
        repository.add(category2)
        val categories = repository.categories.first()
        assertEquals(2, categories.size)
        assertContains(categories, category1)
        assertContains(categories, category2)
    }

    @Test
    fun `update modifies name and memo of an existing category`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Category(name = "Original Name", type = expense, memo = null)
        repository.add(original)
        val updated = original.copy(name = "Updated Name", memo = "new memo")
        repository.update(updated)
        assertEquals(listOf(updated), repository.categories.first())
    }

    @Test
    fun `update modifies the type of an existing category`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val original = Category(name = "Original Name", type = expense, memo = null)
        repository.add(original)
        val updated = original.copy(type = income)
        repository.update(updated)
        assertEquals(listOf(updated), repository.categories.first())
    }

    @Test
    fun `update modifies the parent of an existing category`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val parent = Category(name = "Food", type = expense, memo = null)
        repository.add(parent)
        val original = Category(name = "Groceries", type = expense, memo = null)
        repository.add(original)
        val updated = original.copy(parent = parent.id)
        repository.update(updated)
        assertContains(repository.categories.first(), updated)
    }

    @Test
    fun `update does not affect other categories`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category1 = Category(name = "Category One", type = expense, memo = null)
        val category2 = Category(name = "Category Two", type = expense, memo = null)
        repository.add(category1)
        repository.add(category2)
        val updatedCategory1 = category1.copy(name = "Category One Updated")
        repository.update(updatedCategory1)
        val categories = repository.categories.first()
        assertContains(categories, updatedCategory1)
        assertContains(categories, category2)
    }

    @Test
    fun `removeById removes a category`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category = Category(name = "To Delete", type = expense, memo = null)
        repository.add(category)
        repository.removeById(category.id)
        assertTrue(repository.categories.first().isEmpty())
    }

    @Test
    fun `removeById only removes the specified category`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category1 = Category(name = "Keep", type = expense, memo = null)
        val category2 = Category(name = "Delete", type = expense, memo = null)
        repository.add(category1)
        repository.add(category2)
        repository.removeById(category2.id)
        assertEquals(listOf(category1), repository.categories.first())
    }

    @Test
    fun `removeById of unknown ID leaves categories unchanged`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category = Category(name = "Existing", type = expense, memo = null)
        repository.add(category)
        repository.removeById(CategoryId())
        assertEquals(listOf(category), repository.categories.first())
    }

    @Test
    fun `remove deletes a category by reference`() = runTest {
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))
        val category = Category(name = "To Delete", type = expense, memo = null)
        repository.add(category)
        repository.remove(category)
        assertTrue(repository.categories.first().isEmpty())
    }
}
