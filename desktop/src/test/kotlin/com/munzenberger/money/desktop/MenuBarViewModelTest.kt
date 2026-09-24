package com.munzenberger.money.desktop

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MenuBarViewModelTest {

    private val controller = MoneyRepositoryController()

    @Test
    fun `closes the open repository when exit is selected`() {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)

        MenuBarViewModel(controller).onExitSelected()

        verify { repository.close() }
    }

    @Test
    fun `does nothing when exit is selected with no open repository`() {
        MenuBarViewModel(controller).onExitSelected()
    }

    @Test
    fun `closes the open repository when close database is selected`() {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)

        MenuBarViewModel(controller).onCloseDatabaseSelected()

        verify { repository.close() }
    }

    @Test
    fun `does nothing when close database is selected with no open repository`() {
        MenuBarViewModel(controller).onCloseDatabaseSelected()
    }

    @Test
    fun `state disables closing the repository when there is no open repository`() = runTest {
        val viewModel = MenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(MenuBarUiState(closeRepositoryEnabled = false), awaitItem())
        }
    }

    @Test
    fun `state enables closing the repository once one is connected`() = runTest {
        val viewModel = MenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(MenuBarUiState(closeRepositoryEnabled = false), awaitItem())

            controller.update(mockk<MoneyRepository>())

            assertEquals(MenuBarUiState(closeRepositoryEnabled = true), awaitItem())
        }
    }

    @Test
    fun `state disables closing the repository again once it is cleared`() = runTest {
        val viewModel = MenuBarViewModel(controller)

        viewModel.state.test {
            awaitItem()

            controller.update(mockk<MoneyRepository>())
            awaitItem()

            controller.clear()

            assertEquals(MenuBarUiState(closeRepositoryEnabled = false), awaitItem())
        }
    }
}
