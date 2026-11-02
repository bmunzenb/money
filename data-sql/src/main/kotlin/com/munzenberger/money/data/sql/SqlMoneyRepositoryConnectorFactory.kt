package com.munzenberger.money.data.sql

import com.munzenberger.money.data.api.MoneyRepositoryConnector
import com.munzenberger.money.data.api.MoneyRepositoryConnectorFactory
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.coroutines.CoroutineContext

/** Creates [SqlMoneyRepositoryConnector]s that run their database work in [context]. */
class SqlMoneyRepositoryConnectorFactory(
    private val context: CoroutineContext = Dispatchers.IO,
) : MoneyRepositoryConnectorFactory {

    override fun file(file: File): MoneyRepositoryConnector = SqlMoneyRepositoryConnector(file, context)

    override fun inMemory(): MoneyRepositoryConnector = SqlMoneyRepositoryConnector.inMemory(context)
}
