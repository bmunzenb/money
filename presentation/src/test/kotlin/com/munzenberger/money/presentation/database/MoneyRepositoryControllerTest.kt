package com.munzenberger.money.presentation.database

import app.cash.turbine.test
import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.presentation.MoneyRepositoryControllerFixture
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MoneyRepositoryControllerTest {

    private val file = File("money-test.db")
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

    private val populateDemoData = mockk<PopulateDemoDataUseCase> {
        coEvery { this@mockk(any()) } returns PopulateDemoDataResult.Success
    }

    private fun connector(status: MoneyRepositoryConnectionStatus) =
        mockk<MoneyRepositoryConnector> {
            coEvery { create() } returns status
            coEvery { connect() } returns status
        }

    // Each open/create/demo call uses the next connector, in order.
    private fun controller(vararg connectors: MoneyRepositoryConnector): MoneyRepositoryController {
        val remaining = ArrayDeque(connectors.toList())
        return MoneyRepositoryController(
            fileConnectorFactory = { remaining.removeFirst() },
            demoConnectorFactory = { remaining.removeFirst() },
            populateDemoData = populateDemoData,
            context = EmptyCoroutineContext,
        )
    }

    @Test
    fun testInitialValueIsNull() {
        val controller = controller(connector(MoneyRepositoryConnectionStatus.UnsupportedVersion))

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testOpenDatabaseSetsRepositoryWhenReady() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))

        controller.openDatabase(file)

        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun testOpenDatabaseConnectsToExistingDatabase() = runTest {
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(mockk()))
        val controller = controller(connector)

        controller.openDatabase(file)

        coVerify(exactly = 1) { connector.connect() }
        coVerify(exactly = 0) { connector.create() }
    }

    @Test
    fun testOpenDatabaseLeavesRepositoryNullWhenFailed() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Failed(IllegalStateException())))

        controller.openDatabase(file)

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testOpenDatabaseReplacesAndClosesExistingRepository() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val controller = controller(
            connector(MoneyRepositoryConnectionStatus.Ready(first)),
            connector(MoneyRepositoryConnectionStatus.Ready(second)),
        )

        controller.openDatabase(file)
        controller.openDatabase(file)

        verify(exactly = 1) { first.close() }
        assertSame(second, controller.moneyRepository.value)
    }

    @Test
    fun testOpenDatabaseClosesExistingRepositoryEvenWhenFailed() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val controller = controller(
            connector(MoneyRepositoryConnectionStatus.Ready(first)),
            connector(MoneyRepositoryConnectionStatus.Failed(IllegalStateException())),
        )

        controller.openDatabase(file)
        controller.openDatabase(file)

        verify(exactly = 1) { first.close() }
        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testCreateDatabaseSetsRepositoryWhenReady() = runTest {
        val repository = mockk<MoneyRepository>()
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(repository))
        val controller = controller(connector)

        controller.createDatabase(file)

        coVerify(exactly = 1) { connector.create() }
        coVerify(exactly = 0) { connector.connect() }
        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun testCreateDatabaseDeletesExistingFileBeforeCreating() = runTest {
        val existing = createTempFile()
        var existedAtCreate = true
        val connector = mockk<MoneyRepositoryConnector> {
            coEvery { create() } answers {
                existedAtCreate = existing.exists()
                MoneyRepositoryConnectionStatus.Ready(mockk())
            }
        }
        val controller = controller(connector)

        controller.createDatabase(existing)

        assertFalse(existedAtCreate)
    }

    @Test
    fun testOpenDemoCreatesDatabaseAndSetsRepositoryWhenReady() = runTest {
        val repository = mockk<MoneyRepository>()
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(repository))
        val controller = controller(connector)

        controller.openDemo()

        coVerify(exactly = 1) { connector.create() }
        coVerify(exactly = 0) { connector.connect() }
        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun testOpenDemoPopulatesDemoDataBeforeSettingRepository() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))
        coEvery { populateDemoData(repository) } answers {
            assertNull(controller.moneyRepository.value)
            PopulateDemoDataResult.Success
        }

        controller.openDemo()

        coVerify(exactly = 1) { populateDemoData(repository) }
        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun testOpenDemoOpensInMemoryDatabaseWithBanksAndAccounts() = runTest {
        val controller = MoneyRepositoryControllerFixture().controller

        controller.openDemo()

        val repository = assertNotNull(controller.moneyRepository.value)
        val banks = repository.banks.first()
        val accounts = repository.accounts.first()
        assertTrue(banks.isNotEmpty())
        assertTrue(banks.all { bank -> accounts.any { it.bankId == bank.id } })
        controller.close()
    }

    @Test
    fun testOpenDemoFailsAndClosesRepositoryWhenPopulatingFails() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val error = IllegalStateException("write failed")
        coEvery { populateDemoData(repository) } returns PopulateDemoDataResult.Failure(error)
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))

        val status = controller.openDemo()

        assertEquals(MoneyRepositoryConnectionStatus.Failed(error), status)
        verify(exactly = 1) { repository.close() }
        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testOpenDemoReplacesAndClosesExistingRepository() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val controller = controller(
            connector(MoneyRepositoryConnectionStatus.Ready(first)),
            connector(MoneyRepositoryConnectionStatus.Ready(second)),
        )

        controller.openDatabase(file)
        controller.openDemo()

        verify(exactly = 1) { first.close() }
        assertSame(second, controller.moneyRepository.value)
    }

    @Test
    fun testCloseClosesRepository() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))
        controller.openDatabase(file)

        controller.close()

        verify(exactly = 1) { repository.close() }
    }

    @Test
    fun testCloseResetsToNull() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(mockk(relaxUnitFun = true))))
        controller.openDatabase(file)

        controller.close()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testCloseWithoutRepositoryStaysNull() {
        val controller = controller(connector(MoneyRepositoryConnectionStatus.UnsupportedVersion))

        controller.close()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun testResultFlowEmitsSuccessFromSelectedFlow() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))

        controller.resultFlow { flowOf(1, 2) }.test {
            controller.openDatabase(file)

            assertEquals(Result.success(1), awaitItem())
            assertEquals(Result.success(2), awaitItem())
        }
    }

    @Test
    fun testResultFlowEmitsFailureWhenSelectedFlowThrows() = runTest {
        val error = IllegalStateException("query failed")
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(mockk())))

        controller.resultFlow<Int> { flow { throw error } }.test {
            controller.openDatabase(file)

            assertEquals(Result.failure(error), awaitItem())
        }
    }

    @Test
    fun testResultFlowRecoversWhenNewRepositoryIsConnected() = runTest {
        val failing = mockk<MoneyRepository>(relaxUnitFun = true)
        val working = mockk<MoneyRepository>()
        val error = IllegalStateException("query failed")
        val controller = controller(
            connector(MoneyRepositoryConnectionStatus.Ready(failing)),
            connector(MoneyRepositoryConnectionStatus.Ready(working)),
        )

        controller.resultFlow { repository ->
            if (repository === failing) flow { throw error } else flowOf(1)
        }.test {
            controller.openDatabase(file)
            assertEquals(Result.failure(error), awaitItem())

            controller.openDatabase(file)
            assertEquals(Result.success(1), awaitItem())
        }
    }
}
