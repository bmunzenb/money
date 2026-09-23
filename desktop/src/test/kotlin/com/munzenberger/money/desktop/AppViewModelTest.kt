package com.munzenberger.money.desktop

import androidx.navigation3.runtime.NavBackStack
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

    private fun routeAfter(block: NavBackStack<Route>.() -> Unit): Route {
        val backStack = NavBackStack<Route>()
        backStack.block()
        return backStack.single()
    }

    @Test
    fun `navigates to Welcome when there is no repository`() = runTest {
        navigator.events.test {
            AppViewModel(navigator, controller)

            assertEquals(Route.Welcome, routeAfter(awaitItem().block))
        }
    }

    @Test
    fun `navigates to AccountList when a repository connects`() = runTest {
        navigator.events.test {
            AppViewModel(navigator, controller)
            awaitItem()

            controller.update(mockk<MoneyRepository>())

            assertEquals(Route.AccountList, routeAfter(awaitItem().block))
        }
    }

    @Test
    fun `navigates to Welcome when the repository disconnects`() = runTest {
        navigator.events.test {
            AppViewModel(navigator, controller)
            awaitItem()

            controller.update(mockk<MoneyRepository>())
            awaitItem()

            controller.clear()

            assertEquals(Route.Welcome, routeAfter(awaitItem().block))
        }
    }
}
