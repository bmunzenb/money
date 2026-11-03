package com.munzenberger.money.presentation.database

import com.munzenberger.money.data.api.MoneyRepositoryConnector
import java.io.File

interface MoneyRepositoryConnectorFactory {
    fun create(file: File): MoneyRepositoryConnector
    fun open(file: File): MoneyRepositoryConnector
    fun createInMemory(): MoneyRepositoryConnector
}
