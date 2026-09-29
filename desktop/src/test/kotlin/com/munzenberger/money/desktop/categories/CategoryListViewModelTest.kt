package com.munzenberger.money.desktop.categories

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryListViewModelTest {

    private val categoryType = CategoryType(
        id = CategoryTypeId(1),
        value = CategoryTypeConstant.Expense,
    )

    private fun category(name: String) = Category(name = name, type = categoryType)

    private fun repositoryWithCategories(categories: Flow<List<Category>>) =
        mockk<MoneyRepository> {
            every { this@mockk.categories } returns categories
        }

    @Test
    fun `state emits nothing when there is no repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = CategoryListViewModel(controller)

        viewModel.state.test {
            expectNoEvents()
        }
    }

    @Test
    fun `state emits categories from the connected repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = CategoryListViewModel(controller)
        val categories = listOf(category("Groceries"), category("Rent"))

        viewModel.state.test {
            controller.update(repositoryWithCategories(flowOf(categories)))

            assertEquals(CategoryListUiState(categories = categories), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions from the repository categories flow`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = CategoryListViewModel(controller)
        val groceries = category("Groceries")
        val rent = category("Rent")
        val categoriesFlow = MutableStateFlow(listOf(groceries))

        viewModel.state.test {
            controller.update(repositoryWithCategories(categoriesFlow))
            assertEquals(CategoryListUiState(categories = listOf(groceries)), awaitItem())

            categoriesFlow.value = listOf(groceries, rent)
            assertEquals(CategoryListUiState(categories = listOf(groceries, rent)), awaitItem())
        }
    }

    @Test
    fun `state switches to the newly connected repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = CategoryListViewModel(controller)
        val firstCategories = listOf(category("Groceries"))
        val secondCategories = listOf(category("Rent"))

        viewModel.state.test {
            controller.update(repositoryWithCategories(flowOf(firstCategories)))
            assertEquals(CategoryListUiState(categories = firstCategories), awaitItem())

            controller.update(repositoryWithCategories(flowOf(secondCategories)))
            assertEquals(CategoryListUiState(categories = secondCategories), awaitItem())
        }
    }
}
