package com.munzenberger.money.presentation.database

import app.cash.turbine.test
import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionResult
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private fun connector(result: MoneyRepositoryConnectionResult) =
        mockk<MoneyRepositoryConnector> {
            coEvery { connect() } returns result
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
    fun `openDatabase sets repository when ready`() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))

        controller.openDatabase(file)

        assertEquals(RepositoryConnectionState.Connected(repository), controller.connectionState.value)
    }

    @Test
    fun `openDatabase connects to existing database`() = runTest {
        val connector = connector(MoneyRepositoryConnectionResult.Success(mockk()))
        val controller = controller(connector)

        controller.openDatabase(file)

        coVerify(exactly = 1) { connector.connect() }
        assertEquals(listOf("open"), factoryCalls)
    }

    @Test
    fun `openDatabase leaves the controller disconnected when failed`() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionResult.Failure(IllegalStateException())))

        controller.openDatabase(file)

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `openDatabase replaces and closes existing repository`() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val controller = controller(
            connector(MoneyRepositoryConnectionResult.Success(first)),
            connector(MoneyRepositoryConnectionResult.Success(second)),
        )

        controller.openDatabase(file)
        controller.openDatabase(file)

        verify(exactly = 1) { first.close() }
        assertEquals(RepositoryConnectionState.Connected(second), controller.connectionState.value)
    }

    @Test
    fun `openDatabase closes existing repository even when failed`() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val controller = controller(
            connector(MoneyRepositoryConnectionResult.Success(first)),
            connector(MoneyRepositoryConnectionResult.Failure(IllegalStateException())),
        )

        controller.openDatabase(file)
        controller.openDatabase(file)

        verify(exactly = 1) { first.close() }
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `createDatabase sets repository when ready`() = runTest {
        val repository = mockk<MoneyRepository>()
        val connector = connector(MoneyRepositoryConnectionResult.Success(repository))
        val controller = controller(connector)

        controller.createDatabase(file)

        coVerify(exactly = 1) { connector.connect() }
        assertEquals(listOf("create"), factoryCalls)
        assertEquals(RepositoryConnectionState.Connected(repository), controller.connectionState.value)
    }

    @Test
    fun `createDatabase deletes existing file before creating`() = runTest {
        val existing = createTempFile()
        var existedAtCreate = true
        val connector = mockk<MoneyRepositoryConnector> {
            coEvery { connect() } answers {
                existedAtCreate = existing.exists()
                MoneyRepositoryConnectionResult.Success(mockk())
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
        val connector = connector(MoneyRepositoryConnectionResult.Success(mockk()))
        val controller = controller(connector)

        val result = controller.createDatabase(directory)

        assertIs<IOException>(assertIs<MoneyRepositoryConnectionResult.Failure>(result).cause)
        coVerify(exactly = 0) { connector.connect() }
        assertTrue(factoryCalls.isEmpty())
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `overlapping opens close the first repository`() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val firstConnected = CompletableDeferred<Unit>()
        val slowConnector = mockk<MoneyRepositoryConnector> {
            coEvery { connect() } coAnswers {
                firstConnected.await()
                MoneyRepositoryConnectionResult.Success(first)
            }
        }
        val controller = controller(slowConnector, connector(MoneyRepositoryConnectionResult.Success(second)))

        val opens = listOf(
            launch { controller.openDatabase(file) },
            launch { controller.openDatabase(file) },
        )
        runCurrent()
        firstConnected.complete(Unit)
        opens.joinAll()

        verify(exactly = 1) { first.close() }
        assertEquals(RepositoryConnectionState.Connected(second), controller.connectionState.value)
    }

    @Test
    fun `openDemo creates database and sets repository when ready`() = runTest {
        val repository = mockk<MoneyRepository>()
        val connector = connector(MoneyRepositoryConnectionResult.Success(repository))
        val controller = controller(connector)

        controller.openDemo()

        coVerify(exactly = 1) { connector.connect() }
        assertEquals(listOf("createInMemory"), factoryCalls)
        assertEquals(RepositoryConnectionState.Connected(repository), controller.connectionState.value)
    }

    @Test
    fun `openDemo populates demo data before setting repository`() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))
        coEvery { populateDemoData(repository) } answers {
            assertEquals(RepositoryConnectionState.Connecting, controller.connectionState.value)
            PopulateDemoDataResult.Success
        }

        controller.openDemo()

        coVerify(exactly = 1) { populateDemoData(repository) }
        assertEquals(RepositoryConnectionState.Connected(repository), controller.connectionState.value)
    }

    @Test
    fun `openDemo fails and closes repository when populating fails`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val error = IllegalStateException("write failed")
        coEvery { populateDemoData(repository) } returns PopulateDemoDataResult.Failure(error)
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))

        val result = controller.openDemo()

        assertEquals(MoneyRepositoryConnectionResult.Failure(error), result)
        verify(exactly = 1) { repository.close() }
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `openDemo fails and closes repository when populating throws`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val error = IllegalStateException("unexpected")
        coEvery { populateDemoData(repository) } throws error
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))

        assertEquals(MoneyRepositoryConnectionResult.Failure(error), controller.openDemo())

        verify(exactly = 1) { repository.close() }
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `openDemo replaces and closes existing repository`() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val controller = controller(
            connector(MoneyRepositoryConnectionResult.Success(first)),
            connector(MoneyRepositoryConnectionResult.Success(second)),
        )

        controller.openDatabase(file)
        controller.openDemo()

        verify(exactly = 1) { first.close() }
        assertEquals(RepositoryConnectionState.Connected(second), controller.connectionState.value)
    }

    @Test
    fun `close closes repository`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))
        controller.openDatabase(file)

        controller.close()

        verify(exactly = 1) { repository.close() }
    }

    @Test
    fun `close disconnects`() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(mockk(relaxUnitFun = true))))
        controller.openDatabase(file)

        controller.close()

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `close without repository stays disconnected`() = runTest {
        val controller = controller()

        controller.close()

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `openDatabase connects when the existing repository fails to close`() = runTest {
        val first = mockk<MoneyRepository> { every { close() } throws IllegalStateException("close failed") }
        val second = mockk<MoneyRepository>()
        val controller = controller(
            connector(MoneyRepositoryConnectionResult.Success(first)),
            connector(MoneyRepositoryConnectionResult.Success(second)),
        )
        controller.openDatabase(file)

        controller.openDatabase(file)

        assertEquals(RepositoryConnectionState.Connected(second), controller.connectionState.value)
    }

    @Test
    fun `close disconnects when the repository fails to close`() = runTest {
        val repository = mockk<MoneyRepository> { every { close() } throws IllegalStateException("close failed") }
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))
        controller.openDatabase(file)

        controller.close()

        verify(exactly = 1) { repository.close() }
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `close waits for an open in progress and closes its repository`() = runTest {
        val repository = mockk<MoneyRepository>(relaxUnitFun = true)
        val connected = CompletableDeferred<Unit>()
        val slowConnector = mockk<MoneyRepositoryConnector> {
            coEvery { connect() } coAnswers {
                connected.await()
                MoneyRepositoryConnectionResult.Success(repository)
            }
        }
        val controller = controller(slowConnector)

        val open = launch { controller.openDatabase(file) }
        runCurrent()
        val close = launch { controller.close() }
        runCurrent()
        connected.complete(Unit)
        joinAll(open, close)

        verify(exactly = 1) { repository.close() }
        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `connectionState is initially disconnected`() {
        val controller = controller()

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `connectionState is connecting while an open is in progress, then connected`() = runTest {
        val repository = mockk<MoneyRepository>()
        val connected = CompletableDeferred<Unit>()
        val slowConnector = mockk<MoneyRepositoryConnector> {
            coEvery { connect() } coAnswers {
                connected.await()
                MoneyRepositoryConnectionResult.Success(repository)
            }
        }
        val controller = controller(slowConnector)

        val open = launch { controller.openDatabase(file) }
        runCurrent()

        assertEquals(RepositoryConnectionState.Connecting, controller.connectionState.value)

        connected.complete(Unit)
        open.join()

        assertEquals(RepositoryConnectionState.Connected(repository), controller.connectionState.value)
    }

    @Test
    fun `connectionState is disconnected when an open fails`() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionResult.Failure(IllegalStateException())))

        controller.openDatabase(file)

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `openDatabase fails and is disconnected when an open throws`() = runTest {
        val error = IllegalStateException()
        val controller = controller(
            mockk<MoneyRepositoryConnector> { coEvery { connect() } throws error },
        )

        assertEquals(MoneyRepositoryConnectionResult.Failure(error), controller.openDatabase(file))

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `connectionState goes straight from connected to connecting when replacing a repository`() = runTest {
        val first = mockk<MoneyRepository>(relaxUnitFun = true)
        val second = mockk<MoneyRepository>()
        val controller = controller(
            connector(MoneyRepositoryConnectionResult.Success(first)),
            connector(MoneyRepositoryConnectionResult.Success(second)),
        )
        controller.openDatabase(file)
        val states = mutableListOf<RepositoryConnectionState>()
        val collect = launch(UnconfinedTestDispatcher(testScheduler)) { controller.connectionState.toList(states) }

        controller.openDatabase(file)
        collect.cancel()

        assertEquals(
            listOf(
                RepositoryConnectionState.Connected(first),
                RepositoryConnectionState.Connecting,
                RepositoryConnectionState.Connected(second),
            ),
            states,
        )
    }

    @Test
    fun `connectionState is disconnected after close`() = runTest {
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(mockk(relaxUnitFun = true))))
        controller.openDatabase(file)

        controller.close()

        assertEquals(RepositoryConnectionState.Disconnected, controller.connectionState.value)
    }

    @Test
    fun `resultFlow emits success from selected flow`() = runTest {
        val repository = mockk<MoneyRepository>()
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(repository)))

        controller.resultFlow { flowOf(1, 2) }.test {
            controller.openDatabase(file)

            assertEquals(Result.success(1), awaitItem())
            assertEquals(Result.success(2), awaitItem())
        }
    }

    @Test
    fun `resultFlow emits failure when selected flow throws`() = runTest {
        val error = IllegalStateException("query failed")
        val controller = controller(connector(MoneyRepositoryConnectionResult.Success(mockk())))

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
            connector(MoneyRepositoryConnectionResult.Success(failing)),
            connector(MoneyRepositoryConnectionResult.Success(working)),
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
