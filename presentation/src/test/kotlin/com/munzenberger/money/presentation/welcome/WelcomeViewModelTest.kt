package com.munzenberger.money.presentation.welcome

import app.cash.turbine.test
import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

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
            controller.connectionState.first { it is RepositoryConnectionState.Connected }
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
            controller.connectionState.first { it is RepositoryConnectionState.Connected }
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
            controller.connectionState.first { it is RepositoryConnectionState.Connected }
            assertEquals(WelcomeUiState(isLoading = true), viewModel.state.value)
            expectNoEvents()
        }
    }

    @Test
    fun `state is loading while a database it didn't open is connecting`() = runTest {
        val connected = CompletableDeferred<Unit>()
        val populateDemoData = fixture.populateDemoData
        coEvery { populateDemoData(any()) } coAnswers {
            connected.await()
            PopulateDemoDataResult.Success
        }
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            val open = launch { controller.openDemo() }

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            connected.complete(Unit)
            open.join()
            expectNoEvents()
        }
    }

    @Test
    fun `state hides its error while a database it didn't open is connecting`() = runTest {
        val connected = CompletableDeferred<Unit>()
        val populateDemoData = fixture.populateDemoData
        coEvery { populateDemoData(any()) } coAnswers {
            connected.await()
            PopulateDemoDataResult.Success
        }
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())
            viewModel.createDatabase(fixture.unusableFile())
            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.CreateDatabaseFailed), awaitItem())

            val open = launch { controller.openDemo() }

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            connected.complete(Unit)
            open.join()
            expectNoEvents()
        }
    }

    @Test
    fun `state clears its error when a database it didn't open fails to connect`() = runTest {
        val connected = CompletableDeferred<Unit>()
        val populateDemoData = fixture.populateDemoData
        coEvery { populateDemoData(any()) } coAnswers {
            connected.await()
            PopulateDemoDataResult.Failure(IllegalStateException())
        }
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())
            viewModel.createDatabase(fixture.unusableFile())
            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.CreateDatabaseFailed), awaitItem())

            val open = launch { controller.openDemo() }

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            connected.complete(Unit)
            open.join()
            assertEquals(WelcomeUiState(), awaitItem())
        }
    }

    @Test
    fun `state shows an error when opening the demo database fails`() = runTest {
        val controller = mockk<MoneyRepositoryController> {
            every { connectionState } returns MutableStateFlow(RepositoryConnectionState.Disconnected)
            coEvery { openDemo() } coAnswers {
                yield()
                MoneyRepositoryConnectionStatus.Failed(IllegalStateException())
            }
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

        controller.connectionState.test {
            assertEquals(RepositoryConnectionState.Disconnected, awaitItem())

            viewModel.openDemo()

            assertEquals(RepositoryConnectionState.Connecting, awaitItem())
            val repository = assertNotNull(awaitItem().repository)
            coVerify(exactly = 1) { fixture.populateDemoData(repository) }
        }
    }

    @Test
    fun `state shows an error when opening the demo database throws`() = runTest {
        val populateDemoData = fixture.populateDemoData
        coEvery { populateDemoData(any()) } throws IllegalStateException("unexpected")
        val viewModel = WelcomeViewModel(controller)

        viewModel.state.test {
            assertEquals(WelcomeUiState(), awaitItem())

            viewModel.openDemo()

            assertEquals(WelcomeUiState(isLoading = true), awaitItem())
            assertEquals(WelcomeUiState(error = WelcomeError.OpenDemoFailed), awaitItem())
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

        controller.connectionState.test {
            assertEquals(RepositoryConnectionState.Disconnected, awaitItem())

            viewModel.createDatabase(file)

            assertEquals(RepositoryConnectionState.Connecting, awaitItem())
            assertEquals(RepositoryConnectionState.Connected(assertNotNull(fixture.databaseAt(file))), awaitItem())
        }
    }

    @Test
    fun `closes any open repository before creating a new database`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        fixture.connect(repository)
        val viewModel = WelcomeViewModel(controller)

        controller.connectionState.test {
            assertEquals(RepositoryConnectionState.Connected(repository), awaitItem())

            viewModel.createDatabase(fixture.newFile())

            assertEquals(RepositoryConnectionState.Connecting, awaitItem())
            assertIs<RepositoryConnectionState.Connected>(awaitItem())

            verify { repository.close() }
        }
    }

    @Test
    fun `connects to an existing database when it is opened`() = runTest {
        val existing = mockk<MoneyRepository>()
        val file = fixture.addDatabase(existing)
        val viewModel = WelcomeViewModel(controller)

        controller.connectionState.test {
            assertEquals(RepositoryConnectionState.Disconnected, awaitItem())

            viewModel.openDatabase(file)

            assertEquals(RepositoryConnectionState.Connecting, awaitItem())
            assertEquals(RepositoryConnectionState.Connected(existing), awaitItem())
        }
    }

    @Test
    fun `closes any open repository before opening a database`() = runTest {
        val file = fixture.addDatabase()
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        fixture.connect(repository)
        val viewModel = WelcomeViewModel(controller)

        controller.connectionState.test {
            assertEquals(RepositoryConnectionState.Connected(repository), awaitItem())

            viewModel.openDatabase(file)

            assertEquals(RepositoryConnectionState.Connecting, awaitItem())
            assertIs<RepositoryConnectionState.Connected>(awaitItem())

            verify { repository.close() }
        }
    }
}
