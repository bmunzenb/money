package com.munzenberger.money.desktop.toolbar

import app.cash.turbine.test
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppToolBarViewModelTest {

    private val navigator = Navigator()

    @Test
    fun `state disables back when the back stack has a single entry`() = runTest {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppToolBarUiState(isBackEnabled = false), awaitItem())
        }
    }

    @Test
    fun `state enables back once the back stack has more than one entry`() = runTest {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.state.test {
            assertEquals(AppToolBarUiState(isBackEnabled = false), awaitItem())

            navigator.navigate { add(Route.AccountList) }

            assertEquals(AppToolBarUiState(isBackEnabled = true), awaitItem())
        }
    }

    @Test
    fun `state disables back again once the back stack shrinks to a single entry`() = runTest {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.state.test {
            awaitItem()

            navigator.navigate { add(Route.AccountList) }
            awaitItem()

            navigator.navigate { removeAt(lastIndex) }

            assertEquals(AppToolBarUiState(isBackEnabled = false), awaitItem())
        }
    }

    @Test
    fun `onBackClick pops the most recent route from the back stack`() {
        navigator.navigate { add(Route.AccountList) }
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onBackClick()

        assertEquals(listOf(Route.Welcome), navigator.backStack.toList())
    }

    @Test
    fun `onBackClick does nothing when the back stack has a single entry`() {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onBackClick()

        assertEquals(listOf(Route.Welcome), navigator.backStack.toList())
    }

    @Test
    fun `onAccountsClick adds the AccountList route to the back stack`() {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onAccountsClick()

        assertEquals(listOf(Route.Welcome, Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `onAccountsClick does nothing when the AccountList route is already last`() {
        navigator.navigate { add(Route.AccountList) }
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onAccountsClick()

        assertEquals(listOf(Route.Welcome, Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `onCategoriesClick adds the CategoryList route to the back stack`() {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onCategoriesClick()

        assertEquals(listOf(Route.Welcome, Route.CategoryList), navigator.backStack.toList())
    }

    @Test
    fun `onCategoriesClick does nothing when the CategoryList route is already last`() {
        navigator.navigate { add(Route.CategoryList) }
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onCategoriesClick()

        assertEquals(listOf(Route.Welcome, Route.CategoryList), navigator.backStack.toList())
    }

    @Test
    fun `onPayeesClick adds the PayeeList route to the back stack`() {
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onPayeesClick()

        assertEquals(listOf(Route.Welcome, Route.PayeeList), navigator.backStack.toList())
    }

    @Test
    fun `onPayeesClick does nothing when the PayeeList route is already last`() {
        navigator.navigate { add(Route.PayeeList) }
        val viewModel = AppToolBarViewModel(navigator)

        viewModel.onPayeesClick()

        assertEquals(listOf(Route.Welcome, Route.PayeeList), navigator.backStack.toList())
    }
}
