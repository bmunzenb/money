package com.munzenberger.money.presentation.menu

import app.cash.turbine.test
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.presentation.database.DatabaseError
import com.munzenberger.money.presentation.database.RepositoryConnectionState
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertNotNull

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
            assertEquals(AppMenuBarUiState(canOpenDatabase = false), awaitItem())
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
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `createDatabase connects to a new database in place of the open repository`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        fixture.connect(repository)
        val file = fixture.newFile()
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.createDatabase(file)

        val connected = controller.connectionState.first { it is RepositoryConnectionState.Connected }
        assertEquals(assertNotNull(fixture.databaseAt(file)), connected.repository)
        verify { repository.close() }
    }

    @Test
    fun `openDatabase connects to an existing database`() = runTest {
        val existing = mockk<MoneyRepository>()
        val file = fixture.addDatabase(existing)
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.openDatabase(file)

        val connected = controller.connectionState.first { it is RepositoryConnectionState.Connected }
        assertEquals(existing, connected.repository)
    }

    @Test
    fun `state can't open a database while one is opening`() = runTest {
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(AppMenuBarUiState(), awaitItem())

            viewModel.openDatabase(fixture.addDatabase())

            assertEquals(AppMenuBarUiState(canOpenDatabase = false), awaitItem())
            assertEquals(AppMenuBarUiState(canCloseRepository = true), awaitItem())
        }
    }

    @Test
    fun `state shows an error until it's dismissed when a database fails to open`() = runTest {
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(AppMenuBarUiState(), awaitItem())

            viewModel.openDatabase(fixture.unusableFile())

            assertEquals(AppMenuBarUiState(canOpenDatabase = false), awaitItem())
            assertEquals(AppMenuBarUiState(error = DatabaseError.OpenDatabaseFailed), awaitItem())

            viewModel.dismissError()

            assertEquals(AppMenuBarUiState(), awaitItem())
        }
    }

    @Test
    fun `state shows an error when creating a database fails`() = runTest {
        val viewModel = AppMenuBarViewModel(controller)

        viewModel.state.test {
            assertEquals(AppMenuBarUiState(), awaitItem())

            viewModel.createDatabase(fixture.unusableFile())

            assertEquals(AppMenuBarUiState(canOpenDatabase = false), awaitItem())
            assertEquals(AppMenuBarUiState(error = DatabaseError.CreateDatabaseFailed), awaitItem())
        }
    }
}
