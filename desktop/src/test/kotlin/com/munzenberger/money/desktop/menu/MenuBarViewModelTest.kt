package com.munzenberger.money.desktop.menu

import app.cash.turbine.test
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MenuBarViewModelTest {

    private val controller = MoneyRepositoryController()
    private val tempFiles = mutableListOf<File>()

    @AfterTest
    fun cleanup() {
        tempFiles.forEach { it.delete() }
    }

    private fun createTempFile(): File =
        File.createTempFile("money-test", ".db").also {
            it.deleteOnExit()
            tempFiles.add(it)
        }

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

    @Test
    fun `connects to a newly created database when a new database is selected`() = runTest {
        val file = createTempFile()
        val viewModel = MenuBarViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.onNewDatabaseSelected(file)

            assertNotNull(awaitItem())
        }
    }

    @Test
    fun `closes any open repository before creating a new database`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)
        val viewModel = MenuBarViewModel(controller)

        controller.moneyRepository.test {
            assertEquals(repository, awaitItem())

            viewModel.onNewDatabaseSelected(createTempFile())

            assertNull(awaitItem())
            assertNotNull(awaitItem())

            verify { repository.close() }
        }
    }

    @Test
    fun `connects to an existing database when it is opened`() = runTest {
        val file = createTempFile()
        val created = SqlMoneyRepositoryConnector(file).create()
        check(created is MoneyRepositoryConnectionStatus.Ready)
        created.moneyRepository.close()

        val viewModel = MenuBarViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.onOpenDatabaseSelected(file)

            assertNotNull(awaitItem())
        }
    }

    @Test
    fun `closes any open repository before opening a database`() = runTest {
        val file = createTempFile()
        val created = SqlMoneyRepositoryConnector(file).create()
        check(created is MoneyRepositoryConnectionStatus.Ready)
        created.moneyRepository.close()

        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)
        val viewModel = MenuBarViewModel(controller)

        controller.moneyRepository.test {
            assertEquals(repository, awaitItem())

            viewModel.onOpenDatabaseSelected(file)

            assertNull(awaitItem())
            assertNotNull(awaitItem())

            verify { repository.close() }
        }
    }
}
