package com.munzenberger.money.presentation.database

import app.cash.turbine.test
import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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
            coEvery { connect() } returns status
        }

    // The connector factory methods the controller called, in order.
    private val factoryCalls = mutableListOf<String>()

    // Each open/create/demo call uses the next connector, in order.
    private fun controller(vararg connectors: MoneyRepositoryConnector): MoneyRepositoryController {
        val remaining = ArrayDeque(connectors.toList())
        return MoneyRepositoryController(
            connectorFactory = object : MoneyRepositoryConnectorFactory {
                override fun create(file: File) = remaining.removeFirst().also { factoryCalls += "create" }
                override fun open(file: File) = remaining.removeFirst().also { factoryCalls += "open" }
                override fun createInMemory() = remaining.removeFirst().also { factoryCalls += "createInMemory" }
            },
            populateDemoData = populateDemoData,
            context = EmptyCoroutineContext,
        )
    }

    @Test
    fun `moneyRepository is initially null`() {
        val controller = controller()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun `openDatabase sets repository when ready`() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))

        controller.openDatabase(file)

        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun `openDatabase connects to existing database`() = runTest {
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(mockk()))
        val controller = controller(connector)

        controller.openDatabase(file)

        coVerify(exactly = 1) { connector.connect() }
        assertEquals(listOf("open"), factoryCalls)
    }

    @Test
    fun `openDatabase leaves repository null when failed`() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Failed(IllegalStateException())))

        controller.openDatabase(file)

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun `openDatabase replaces and closes existing repository`() = runTest {
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
    fun `openDatabase closes existing repository even when failed`() = runTest {
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
    fun `createDatabase sets repository when ready`() = runTest {
        val repository = mockk<MoneyRepository>()
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(repository))
        val controller = controller(connector)

        controller.createDatabase(file)

        coVerify(exactly = 1) { connector.connect() }
        assertEquals(listOf("create"), factoryCalls)
        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun `createDatabase deletes existing file before creating`() = runTest {
        val existing = createTempFile()
        var existedAtCreate = true
        val connector = mockk<MoneyRepositoryConnector> {
            coEvery { connect() } answers {
                existedAtCreate = existing.exists()
                MoneyRepositoryConnectionStatus.Ready(mockk())
            }
        }
        val controller = controller(connector)

        controller.createDatabase(existing)

        assertFalse(existedAtCreate)
    }

    @Test
    fun `createDatabase fails without creating when existing file can't be deleted`() = runTest {
        // A directory that isn't empty can't be deleted.
        val directory = Files.createTempDirectory("money-test").toFile()
        val child = File(directory, "child").apply { createNewFile() }
        tempFiles += listOf(child, directory)
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(mockk()))
        val controller = controller(connector)

        val status = controller.createDatabase(directory)

        assertIs<IOException>(assertIs<MoneyRepositoryConnectionStatus.Failed>(status).error)
        coVerify(exactly = 0) { connector.connect() }
        assertTrue(factoryCalls.isEmpty())
        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun `overlapping opens close the first repository`() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val firstConnected = CompletableDeferred<Unit>()
        val slowConnector = mockk<MoneyRepositoryConnector> {
            coEvery { connect() } coAnswers {
                firstConnected.await()
                MoneyRepositoryConnectionStatus.Ready(first)
            }
        }
        val controller = controller(slowConnector, connector(MoneyRepositoryConnectionStatus.Ready(second)))

        val opens = listOf(
            launch { controller.openDatabase(file) },
            launch { controller.openDatabase(file) },
        )
        runCurrent()
        firstConnected.complete(Unit)
        opens.joinAll()

        verify(exactly = 1) { first.close() }
        assertSame(second, controller.moneyRepository.value)
    }

    @Test
    fun `openDemo creates database and sets repository when ready`() = runTest {
        val repository = mockk<MoneyRepository>()
        val connector = connector(MoneyRepositoryConnectionStatus.Ready(repository))
        val controller = controller(connector)

        controller.openDemo()

        coVerify(exactly = 1) { connector.connect() }
        assertEquals(listOf("createInMemory"), factoryCalls)
        assertSame(repository, controller.moneyRepository.value)
    }

    @Test
    fun `openDemo populates demo data before setting repository`() = runTest {
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
    fun `openDemo fails and closes repository when populating fails`() = runTest {
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
    fun `openDemo closes repository when populating throws`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        coEvery { populateDemoData(repository) } throws IllegalStateException("unexpected")
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))

        assertFailsWith<IllegalStateException> { controller.openDemo() }

        verify(exactly = 1) { repository.close() }
        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun `openDemo replaces and closes existing repository`() = runTest {
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
    fun `close closes repository`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))
        controller.openDatabase(file)

        controller.close()

        verify(exactly = 1) { repository.close() }
    }

    @Test
    fun `close resets to null`() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(mockk(relaxUnitFun = true))))
        controller.openDatabase(file)

        controller.close()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun `close without repository stays null`() {
        val controller = controller()

        controller.close()

        assertNull(controller.moneyRepository.value)
    }

    @Test
    fun `resultFlow emits success from selected flow`() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(repository)))

        controller.resultFlow { flowOf(1, 2) }.test {
            controller.openDatabase(file)

            assertEquals(Result.success(1), awaitItem())
            assertEquals(Result.success(2), awaitItem())
        }
    }

    @Test
    fun `resultFlow emits failure when selected flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        val controller = controller(connector(MoneyRepositoryConnectionStatus.Ready(mockk())))

        controller.resultFlow<Int> { flow { throw error } }.test {
            controller.openDatabase(file)

            assertEquals(Result.failure(error), awaitItem())
        }
    }

    @Test
    fun `resultFlow recovers when new repository is connected`() = runTest {
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
