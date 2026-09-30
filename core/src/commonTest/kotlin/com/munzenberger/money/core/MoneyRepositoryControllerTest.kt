package com.munzenberger.money.core

import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

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

    private fun connector(status: MoneyRepositoryConnectionStatus) =
        mockk<MoneyRepositoryConnector> {
            coEvery { create() } returns status
            coEvery { connect() } returns status
        }

    // Each open/create call uses the next connector, in order.
    private fun controller(vararg connectors: MoneyRepositoryConnector): MoneyRepositoryController {
        val remaining = ArrayDeque(connectors.toList())
        return MoneyRepositoryController(
            connectorFactory = { remaining.removeFirst() },
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
}
