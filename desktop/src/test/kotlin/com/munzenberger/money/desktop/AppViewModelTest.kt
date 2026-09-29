package com.munzenberger.money.desktop

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.navigation.Route
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val navigator = Navigator()
    private val controller = MoneyRepositoryController()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `navigates to Welcome when there is no repository`() = runTest {
        AppViewModel(navigator, controller)

        assertEquals(listOf(Route.Welcome), navigator.backStack.toList())
    }

    @Test
    fun `navigates to AccountList when a repository connects`() = runTest {
        AppViewModel(navigator, controller)

        controller.update(mockk<MoneyRepository>())

        assertEquals(listOf(Route.AccountList), navigator.backStack.toList())
    }

    @Test
    fun `navigates to Welcome when the repository disconnects`() = runTest {
        AppViewModel(navigator, controller)

        controller.update(mockk<MoneyRepository>())
        controller.clear()

        assertEquals(listOf(Route.Welcome), navigator.backStack.toList())
    }

    @Test
    fun `state reports no repository connected initially`() = runTest {
        val viewModel = AppViewModel(navigator, controller)

        viewModel.state.test {
            assertEquals(AppUiState(isRepositoryConnected = false), awaitItem())
        }
    }

    @Test
    fun `state reports a repository connected once one connects`() = runTest {
        val viewModel = AppViewModel(navigator, controller)

        viewModel.state.test {
            assertEquals(AppUiState(isRepositoryConnected = false), awaitItem())

            controller.update(mockk<MoneyRepository>())

            assertEquals(AppUiState(isRepositoryConnected = true), awaitItem())
        }
    }

    @Test
    fun `state reports no repository connected again once it disconnects`() = runTest {
        val viewModel = AppViewModel(navigator, controller)

        viewModel.state.test {
            awaitItem()

            controller.update(mockk<MoneyRepository>())
            awaitItem()

            controller.clear()

            assertEquals(AppUiState(isRepositoryConnected = false), awaitItem())
        }
    }
}
