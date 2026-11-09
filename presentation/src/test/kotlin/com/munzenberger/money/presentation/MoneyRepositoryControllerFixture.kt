package com.munzenberger.money.presentation

import com.munzenberger.money.core.demo.PopulateDemoDataResult
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionResult
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import com.munzenberger.money.presentation.database.MoneyRepositoryConnectorFactory
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Provides a [MoneyRepositoryController] whose connectors are fakes, so tests don't touch a real database.
 *
 * Each file is a fake database: creating one at a [newFile] or opening one from [addDatabase] succeeds, opening a
 * file with no database fails, and both fail for an [unusableFile]. The demo database is a fake repository, and
 * [populateDemoData] is a mock that succeeds. [connect] connects the controller to a given repository.
 */
class MoneyRepositoryControllerFixture {

    // Doesn't exist, so nothing is ever written there (creating a database first deletes any file at its path).
    private val directory = File("fake-databases-${System.nanoTime()}")
    private val databases = mutableMapOf<File, MoneyRepository>()
    private val unusableFiles = mutableSetOf<File>()
    private var fileCount = 0

    val populateDemoData = mockk<PopulateDemoDataUseCase> {
        coEvery { this@mockk(any()) } returns PopulateDemoDataResult.Success
    }

    val controller = MoneyRepositoryController(
        connectorFactory = object : MoneyRepositoryConnectorFactory {
            override fun create(file: File) = FakeConnector {
                if (file in unusableFiles) {
                    MoneyRepositoryConnectionResult.Failure(IOException("Can't create a database at $file"))
                } else {
                    MoneyRepositoryConnectionResult.Success(fakeRepository().also { databases[file] = it })
                }
            }

            override fun open(file: File) = FakeConnector {
                databases[file]?.let { MoneyRepositoryConnectionResult.Success(it) }
                    ?: MoneyRepositoryConnectionResult.Failure(FileNotFoundException("No database at $file"))
            }

            override fun createInMemory() = FakeConnector { MoneyRepositoryConnectionResult.Success(fakeRepository()) }
        },
        populateDemoData = populateDemoData,
        context = EmptyCoroutineContext,
    )

    /** Returns a new file with no database yet. */
    fun newFile(): File = File(directory, "database-${fileCount++}.db")

    /** Returns a new file holding a database whose repository is [repository]. */
    fun addDatabase(repository: MoneyRepository = fakeRepository()): File =
        newFile().also { databases[it] = repository }

    /** Returns a new file where creating or opening a database fails. */
    fun unusableFile(): File = newFile().also { unusableFiles += it }

    /** Returns the repository of the database at [file], or `null` if there's none. */
    fun databaseAt(file: File): MoneyRepository? = databases[file]

    suspend fun connect(repository: MoneyRepository) {
        controller.openDatabase(addDatabase(repository))
    }

    private fun fakeRepository() = mockk<MoneyRepository>(relaxUnitFun = true)

    private class FakeConnector(
        private val connect: () -> MoneyRepositoryConnectionResult,
    ) : MoneyRepositoryConnector {
        // Suspends like a real connect, so collectors see the controller's state while it's connecting.
        override suspend fun connect(): MoneyRepositoryConnectionResult {
            yield()
            return connect.invoke()
        }
    }
}
