package com.munzenberger.money.presentation.toolbar

import app.cash.turbine.test
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
