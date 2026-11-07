package com.munzenberger.money.presentation.menu

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import io.mockk.mockk
import io.mockk.verify
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AppMenuBarViewModelTest {

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
    fun `state reports it can't close a repository while none is open`() = runTest {
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(AppMenuBarUiState(canCloseRepository = false), awaitItem())
        }
    }

    @Test
    fun `state reports it can close a repository once one is open`() = runTest {
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(AppMenuBarUiState(canCloseRepository = false), awaitItem())

            fixture.connect(mockk<MoneyRepository>(relaxUnitFun = true))
            assertEquals(AppMenuBarUiState(canCloseRepository = true), awaitItem())

            controller.close()
            assertEquals(AppMenuBarUiState(canCloseRepository = false), awaitItem())
        }
    }

    @Test
    fun `onCloseRepositoryClick closes the open repository`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        fixture.connect(repository)
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.onCloseRepositoryClick()

        verify { repository.close() }
        assertNull(controller.moneyRepository.value)
    }
}
