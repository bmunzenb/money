package com.munzenberger.money.desktop.rail

import app.cash.turbine.test
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigationRailViewModelTest {

    private val navigator = Navigator()

    @Test
    fun `state has no selection when the current route is not a top-level destination`() = runTest {
        val viewModel = AppNavigationRailViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppNavigationRailUiState(selected = null), awaitItem())
        }
    }

    @Test
    fun `state selects the destination matching the current route`() = runTest {
        navigator.navigate { add(Route.CategoryList) }
        val viewModel = AppNavigationRailViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppNavigationRailUiState(selected = TopLevelDestination.Categories), awaitItem())
        }
    }

    @Test
    fun `state updates the selection when the current route changes`() = runTest {
        val viewModel = AppNavigationRailViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppNavigationRailUiState(selected = null), awaitItem())

            navigator.navigate { add(Route.AccountList) }
            assertEquals(AppNavigationRailUiState(selected = TopLevelDestination.Accounts), awaitItem())

            navigator.navigate { add(Route.PayeeList) }
            assertEquals(AppNavigationRailUiState(selected = TopLevelDestination.Payees), awaitItem())
        }
    }

    @Test
    fun `onDestinationClick replaces the back stack with the destination route`() {
        navigator.navigate { add(Route.AccountList) }
        val viewModel = AppNavigationRailViewModel(navigator)

        viewModel.onDestinationClick(TopLevelDestination.Payees)

        assertEquals(listOf(Route.PayeeList), navigator.backStack.toList())
    }

    @Test
    fun `onDestinationClick does nothing when the destination route is already current`() {
        navigator.navigate { add(Route.CategoryList) }
        val viewModel = AppNavigationRailViewModel(navigator)

        viewModel.onDestinationClick(TopLevelDestination.Categories)

        assertEquals(listOf(Route.Welcome, Route.CategoryList), navigator.backStack.toList())
    }
}
