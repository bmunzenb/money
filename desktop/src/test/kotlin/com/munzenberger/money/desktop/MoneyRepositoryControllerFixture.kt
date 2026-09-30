package com.munzenberger.money.desktop

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.MoneyRepositoryConnectionStatus
import com.munzenberger.money.data.api.MoneyRepositoryConnector
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Provides a [MoneyRepositoryController] for tests. Files are opened and created as real SQLite
 * databases, except that [connect] connects the controller to a given (typically mock) repository.
 */
class MoneyRepositoryControllerFixture {

    private val repositories = mutableMapOf<File, MoneyRepository>()

    val controller = MoneyRepositoryController(
        connectorFactory = { file ->
            repositories[file]?.let(::StubConnector) ?: SqlMoneyRepositoryConnector(file)
        },
        context = EmptyCoroutineContext,
    )

    suspend fun connect(repository: MoneyRepository) {
        val file = File("stub-repository-${repositories.size}")
        repositories[file] = repository
        controller.openDatabase(file)
    }

    private class StubConnector(private val repository: MoneyRepository) : MoneyRepositoryConnector {
        override suspend fun create() = MoneyRepositoryConnectionStatus.Ready(repository)
        override suspend fun connect() = MoneyRepositoryConnectionStatus.Ready(repository)
    }
}
