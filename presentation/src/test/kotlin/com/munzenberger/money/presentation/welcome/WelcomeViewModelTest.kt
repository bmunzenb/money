package com.munzenberger.money.presentation.welcome

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class WelcomeViewModelTest {

    private val fixture = MoneyRepositoryControllerFixture()
    private val controller = fixture.controller

    @Test
    fun `state starts as idle`() = runTest {
        val viewModel = WelcomeViewModel(controller)

        assertEquals(WelcomeUiState(), viewModel.state.value)
    }

    @Test
    fun `state stays loading after a database is created successfully`() = runTest {
        val file = fixture.newFile()
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.createDatabase(file)

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())

            // The screen is about to be replaced, so it keeps showing progress instead of its buttons.
            controller.moneyRepository.first { it != null }
            assertEquals(WelcomeUiState(isLoading = true), viewModel.state.value)
            expectNoEvents()
        }
    }

    @Test
    fun `state stays loading after a database is opened successfully`() = runTest {
        val file = fixture.addDatabase()
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.openDatabase(file)

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())

            // The screen is about to be replaced, so it keeps showing progress instead of its buttons.
            controller.moneyRepository.first { it != null }
            assertEquals(WelcomeUiState(isLoading = true), viewModel.state.value)
            expectNoEvents()
        }
    }

    @Test
    fun `state stays loading after the demo database is opened successfully`() = runTest {
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.openDemo()

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())

            // The screen is about to be replaced, so it keeps showing progress instead of its buttons.
            controller.moneyRepository.first { it != null }
            assertEquals(WelcomeUiState(isLoading = true), viewModel.state.value)
            expectNoEvents()
        }
    }

    @Test
    fun `state shows an error when opening the demo database fails`() = runTest {
        val controller = mockk<MoneyRepositoryController> {
            coEvery { openDemo() } returns MoneyRepositoryConnectionStatus.Failed(IllegalStateException())
        }
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.openDemo()

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.OpenDemoFailed), awaitItem())
        }
    }

    @Test
    fun `connects to a demo database populated with demo data when the demo is opened`() = runTest {
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.openDemo()

            val repository = assertNotNull(awaitItem())
            coVerify(exactly = 1) { fixture.populateDemoData(repository) }
        }
    }

    @Test
    fun `state shows an error when creating a database fails`() = runTest {
        val file = fixture.unusableFile()
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.createDatabase(file)

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.CreateDatabaseFailed), awaitItem())
        }
    }

    @Test
    fun `dismissing an error clears it`() = runTest {
        val file = fixture.unusableFile()
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.createDatabase(file)
            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.CreateDatabaseFailed), awaitItem())

            viewModel.dismissError()

            assertEquals(WelcomeUiState(), awaitItem())
        }
    }

    @Test
    fun `starting another operation clears an error`() = runTest {
        val file = fixture.unusableFile()
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.createDatabase(file)
            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.CreateDatabaseFailed), awaitItem())

            viewModel.openDatabase(fixture.addDatabase())

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `connects to a newly created database when a database is created`() = runTest {
        val file = fixture.newFile()
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.createDatabase(file)

            val repository = assertNotNull(awaitItem())
            assertSame(fixture.databaseAt(file), repository)
        }
    }

    @Test
    fun `closes any open repository before creating a new database`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        fixture.connect(repository)
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertEquals(repository, awaitItem())

            viewModel.createDatabase(fixture.newFile())

            assertNull(awaitItem())
            assertNotNull(awaitItem())

            verify { repository.close() }
        }
    }

    @Test
    fun `connects to an existing database when it is opened`() = runTest {
        val existing = mockk<MoneyRepository>()
        val file = fixture.addDatabase(existing)
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.openDatabase(file)

            assertSame(existing, awaitItem())
        }
    }

    @Test
    fun `closes any open repository before opening a database`() = runTest {
        val file = fixture.addDatabase()
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        fixture.connect(repository)
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertEquals(repository, awaitItem())

            viewModel.openDatabase(file)

            assertNull(awaitItem())
            assertNotNull(awaitItem())

            verify { repository.close() }
        }
    }
}
