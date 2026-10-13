package com.munzenberger.money.desktop.payees

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.desktop.MoneyRepositoryControllerFixture
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PayeeListViewModelTest {

    private fun payee(name: String) = Payee(name = name)

    private fun repositoryWithPayees(payees: Flow<List<Payee>>) =
        mockk<MoneyRepository>(relaxUnitFun = true) {
            every { this@mockk.payees } returns payees
        }

    @Test
    fun `state emits nothing when there is no repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = PayeeListViewModel(fixture.controller)

        viewModel.state.test {
            expectNoEvents()
        }
    }

    @Test
    fun `state emits payees from the connected repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = PayeeListViewModel(fixture.controller)
        val payees = listOf(payee("Grocery Store"), payee("Electric Company"))

        viewModel.state.test {
            fixture.connect(repositoryWithPayees(flowOf(payees)))

            assertEquals(PayeeListUiState.Content(payees = payees), awaitItem())
        }
    }

    @Test
    fun `state reflects subsequent emissions from the repository payees flow`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = PayeeListViewModel(fixture.controller)
        val groceryStore = payee("Grocery Store")
        val electricCompany = payee("Electric Company")
        val payeesFlow = MutableStateFlow(listOf(groceryStore))

        viewModel.state.test {
            fixture.connect(repositoryWithPayees(payeesFlow))
            assertEquals(PayeeListUiState.Content(payees = listOf(groceryStore)), awaitItem())

            payeesFlow.value = listOf(groceryStore, electricCompany)
            assertEquals(PayeeListUiState.Content(payees = listOf(groceryStore, electricCompany)), awaitItem())
        }
    }

    @Test
    fun `state switches to the newly connected repository`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = PayeeListViewModel(fixture.controller)
        val firstPayees = listOf(payee("Grocery Store"))
        val secondPayees = listOf(payee("Electric Company"))

        viewModel.state.test {
            fixture.connect(repositoryWithPayees(flowOf(firstPayees)))
            assertEquals(PayeeListUiState.Content(payees = firstPayees), awaitItem())

            fixture.connect(repositoryWithPayees(flowOf(secondPayees)))
            assertEquals(PayeeListUiState.Content(payees = secondPayees), awaitItem())
        }
    }

    @Test
    fun `state emits error when the repository payees flow throws`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = PayeeListViewModel(fixture.controller)

        viewModel.state.test {
            fixture.connect(repositoryWithPayees(flow { throw IllegalStateException("query failed") }))

            assertEquals(PayeeListUiState.Error, awaitItem())
        }
    }

    @Test
    fun `state recovers from an error when a new repository is connected`() = runTest {
        val fixture = MoneyRepositoryControllerFixture()
        val viewModel = PayeeListViewModel(fixture.controller)
        val payees = listOf(payee("Grocery Store"))

        viewModel.state.test {
            fixture.connect(repositoryWithPayees(flow { throw IllegalStateException("query failed") }))
            assertEquals(PayeeListUiState.Error, awaitItem())

            fixture.connect(repositoryWithPayees(flowOf(payees)))
            assertEquals(PayeeListUiState.Content(payees = payees), awaitItem())
        }
    }
}
