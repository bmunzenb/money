package com.munzenberger.money.presentation

import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import com.munzenberger.money.data.sql.ExistingFileSqlMoneyRepositoryConnector
import com.munzenberger.money.data.sql.InMemorySqlMoneyRepositoryConnector
import com.munzenberger.money.data.sql.NewFileSqlMoneyRepositoryConnector
import com.munzenberger.money.presentation.database.MoneyRepositoryConnectorFactory
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Provides a [MoneyRepositoryController] for tests. Files are opened and created as real SQLite
 * databases, except that [connect] connects the controller to a given (typically mock) repository.
 */
class MoneyRepositoryControllerFixture {

    private val repositories = mutableMapOf<File, MoneyRepository>()

    val controller = MoneyRepositoryController(
        connectorFactory = object : MoneyRepositoryConnectorFactory {
            override fun create(file: File) = NewFileSqlMoneyRepositoryConnector(file, EmptyCoroutineContext)

            override fun open(file: File) =
                repositories[file]?.let(::StubConnector)
                    ?: ExistingFileSqlMoneyRepositoryConnector(file, EmptyCoroutineContext)

            override fun createInMemory() = InMemorySqlMoneyRepositoryConnector(EmptyCoroutineContext)
        },
        populateDemoData = PopulateDemoDataUseCase(),
        context = EmptyCoroutineContext,
    )

    suspend fun connect(repository: MoneyRepository) {
        val file = File("stub-repository-${repositories.size}")
        repositories[file] = repository
        controller.openDatabase(file)
    }

    private class StubConnector(private val repository: MoneyRepository) : MoneyRepositoryConnector {
        override suspend fun connect() = MoneyRepositoryConnectionStatus.Ready(repository)
    }
}
