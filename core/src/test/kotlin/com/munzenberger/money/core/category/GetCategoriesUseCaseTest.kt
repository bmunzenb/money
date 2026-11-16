package com.munzenberger.money.core.category

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryId
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCategoriesUseCaseTest {

    private val expense = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense)

    private val utilities = Category(name = "Utilities", type = expense)
    private val electric = Category(name = "Electric", parentId = utilities.id, type = expense)
    private val groceries = Category(name = "Groceries", type = expense)
    private val orphan = Category(name = "Orphan", parentId = CategoryId(), type = expense)

    private val categories = MutableStateFlow(listOf(electric, utilities, groceries))

    private val repository = mockk<MoneyRepository> {
        every { categories } returns this@GetCategoriesUseCaseTest.categories
    }

    private val getCategories = GetCategoriesUseCase()

    @Test
    fun `emits the categories with their parents`() = runTest {
        getCategories(repository).test {
            assertEquals(
                Result.success(
                    listOf(
                        CategoryWithParent(electric, utilities),
                        CategoryWithParent(utilities, null),
                        CategoryWithParent(groceries, null),
                    )
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `category whose parent isn't in the repository has no parent`() = runTest {
        categories.value = listOf(orphan)

        getCategories(repository).test {
            assertEquals(Result.success(listOf(CategoryWithParent(orphan, null))), awaitItem())
        }
    }

    @Test
    fun `emits no categories when there are none`() = runTest {
        categories.value = emptyList()

        getCategories(repository).test {
            assertEquals(Result.success(emptyList()), awaitItem())
        }
    }

    @Test
    fun `emits new parents when categories change`() = runTest {
        val renamed = utilities.copy(name = "Bills")

        getCategories(repository).test {
            awaitItem()

            categories.value = listOf(electric, renamed)

            assertEquals(
                Result.success(listOf(CategoryWithParent(electric, renamed), CategoryWithParent(renamed, null))),
                awaitItem(),
            )
        }
    }

    @Test
    fun `emits failure when categories flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        every { repository.categories } returns flow { throw error }

        getCategories(repository).test {
            assertEquals(error.message, awaitItem().exceptionOrNull()?.message)
            awaitComplete()
        }
    }
}
