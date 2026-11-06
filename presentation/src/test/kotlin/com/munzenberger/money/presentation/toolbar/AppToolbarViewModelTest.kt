package com.munzenberger.money.presentation.toolbar

import app.cash.turbine.test
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppToolbarViewModelTest {

    private val navigator = Navigator()

    @Test
    fun `state reports it can't navigate back with a single route`() = runTest {
        val viewModel = AppToolbarViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppToolbarUiState(canNavigateBack = false), awaitItem())
        }
    }

    @Test
    fun `state reports it can navigate back once a route is pushed`() = runTest {
        val viewModel = AppToolbarViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppToolbarUiState(canNavigateBack = false), awaitItem())

            navigator.navigate { add(Route.AccountList) }

            assertEquals(AppToolbarUiState(canNavigateBack = true), awaitItem())
        }
    }

    @Test
    fun `onBackClick pops the last route`() {
        val viewModel = AppToolbarViewModel(navigator)
        navigator.navigate { add(Route.AccountList) }

        viewModel.onBackClick()

        assertEquals(listOf(Route.Welcome), navigator.backStack.value)
    }

    @Test
    fun `onBackClick never pops the last route`() {
        val viewModel = AppToolbarViewModel(navigator)

        viewModel.onBackClick()

        assertEquals(listOf(Route.Welcome), navigator.backStack.value)
    }

    @Test
    fun `onNewTransactionClick pushes the new transaction route without an account`() {
        val viewModel = AppToolbarViewModel(navigator)
        navigator.navigate { add(Route.AccountList) }

        viewModel.onNewTransactionClick()

        assertEquals(
            listOf(Route.Welcome, Route.AccountList, Route.NewTransaction(accountId = null)),
            navigator.backStack.value,
        )
    }

    @Test
    fun `state reports it can't add a transaction while the new transaction form is showing`() = runTest {
        val viewModel = AppToolbarViewModel(navigator)

        viewModel.state.test {
            assertTrue(awaitItem().canAddTransaction)

            navigator.navigate { add(Route.NewTransaction()) }
            assertFalse(awaitItem().canAddTransaction)

            navigator.navigate { add(Route.AccountList) }
            assertTrue(awaitItem().canAddTransaction)
        }
    }

    @Test
    fun `onNewTransactionClick does nothing while the new transaction form is showing`() = runTest {
        navigator.navigate { add(Route.NewTransaction(accountId = null)) }
        val viewModel = AppToolbarViewModel(navigator)

        viewModel.state.test {
            assertFalse(awaitItem().canAddTransaction)

            viewModel.onNewTransactionClick()
        }

        assertEquals(listOf(Route.Welcome, Route.NewTransaction(accountId = null)), navigator.backStack.value)
    }
}
