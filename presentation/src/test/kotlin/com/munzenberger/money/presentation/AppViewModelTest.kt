package com.munzenberger.money.presentation

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.navigation.Route
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val fixture = MoneyRepositoryControllerFixture()
    private val controller = fixture.controller

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

        assertEquals(listOf(Route.Welcome), navigator.backStack.value)
    }

    @Test
    fun `navigates to AccountList when a repository connects`() = runTest {
        AppViewModel(navigator, controller)

        fixture.connect(mockk<MoneyRepository>(relaxUnitFun = true))

        assertEquals(listOf(Route.AccountList), navigator.backStack.value)
    }

    @Test
    fun `navigates to Welcome when the repository disconnects`() = runTest {
        AppViewModel(navigator, controller)

        fixture.connect(mockk<MoneyRepository>(relaxUnitFun = true))
        controller.close()

        assertEquals(listOf(Route.Welcome), navigator.backStack.value)
    }

    @Test
    fun `navigates to AccountList when another repository replaces the open one`() = runTest {
        val first = mockk<MoneyRepository>()
        val second = mockk<MoneyRepository>()
        // Goes straight from one repository to the other, as a collector that misses Connecting sees it.
        val connectionState = MutableStateFlow<RepositoryConnectionState>(RepositoryConnectionState.Connected(first))
        val controller = mockk<MoneyRepositoryController> {
            every { this@mockk.connectionState } returns connectionState
        }
        AppViewModel(navigator, controller)
        navigator.navigate { add(Route.AccountSettings(AccountId())) }

        connectionState.value = RepositoryConnectionState.Connected(second)

        assertEquals(listOf(Route.AccountList), navigator.backStack.value)
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

            fixture.connect(mockk<MoneyRepository>(relaxUnitFun = true))

            assertEquals(AppUiState(isRepositoryConnected = true), awaitItem())
        }
    }

    @Test
    fun `state reports no repository connected again once it disconnects`() = runTest {
        val viewModel = AppViewModel(navigator, controller)

        viewModel.state.test {
            awaitItem()

            fixture.connect(mockk<MoneyRepository>(relaxUnitFun = true))
            awaitItem()

            controller.close()

            assertEquals(AppUiState(isRepositoryConnected = false), awaitItem())
        }
    }
}
