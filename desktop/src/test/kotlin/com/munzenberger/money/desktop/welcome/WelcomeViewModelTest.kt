package com.munzenberger.money.desktop.welcome

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

class WelcomeViewModelTest {

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
    fun `connects to a newly created database when a database is created`() = runTest {
        val file = createTempFile()
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.createDatabase(file)

            assertNotNull(awaitItem())
        }
    }

    @Test
    fun `closes any open repository before creating a new database`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        controller.update(repository)
        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertEquals(repository, awaitItem())

            viewModel.createDatabase(createTempFile())

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

        val viewModel = WelcomeViewModel(controller)

        controller.moneyRepository.test {
            assertNull(awaitItem())

            viewModel.openDatabase(file)

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
