package com.munzenberger.money.desktop.payees

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.payee.Payee
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PayeeListViewModelTest {

    private fun payee(name: String) = Payee(name = name)

    private fun repositoryWithPayees(payees: Flow<List<Payee>>) =
        mockk<MoneyRepository> {
            every { this@mockk.payees } returns payees
        }

    @Test
    fun `state emits nothing when there is no repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = PayeeListViewModel(controller)

        viewModel.state.test {
            expectNoEvents()
        }
    }

    @Test
    fun `state emits payees from the connected repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = PayeeListViewModel(controller)
        val payees = listOf(payee("Grocery Store"), payee("Electric Company"))

        viewModel.state.test {
            controller.update(repositoryWithPayees(flowOf(payees)))

            assertEquals(PayeeListUiState.Content(payees = payees), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions from the repository payees flow`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = PayeeListViewModel(controller)
        val groceryStore = payee("Grocery Store")
        val electricCompany = payee("Electric Company")
        val payeesFlow = MutableStateFlow(listOf(groceryStore))

        viewModel.state.test {
            controller.update(repositoryWithPayees(payeesFlow))
            assertEquals(PayeeListUiState.Content(payees = listOf(groceryStore)), awaitItem())

            payeesFlow.value = listOf(groceryStore, electricCompany)
            assertEquals(PayeeListUiState.Content(payees = listOf(groceryStore, electricCompany)), awaitItem())
        }
    }

    @Test
    fun `state switches to the newly connected repository`() = runTest {
        val controller = MoneyRepositoryController()
        val viewModel = PayeeListViewModel(controller)
        val firstPayees = listOf(payee("Grocery Store"))
        val secondPayees = listOf(payee("Electric Company"))

        viewModel.state.test {
            controller.update(repositoryWithPayees(flowOf(firstPayees)))
            assertEquals(PayeeListUiState.Content(payees = firstPayees), awaitItem())

            controller.update(repositoryWithPayees(flowOf(secondPayees)))
            assertEquals(PayeeListUiState.Content(payees = secondPayees), awaitItem())
        }
    }
}
