package com.munzenberger.money.desktop.inject

import com.munzenberger.money.data.sql.ExistingFileSqlMoneyRepositoryConnector
import com.munzenberger.money.data.sql.InMemorySqlMoneyRepositoryConnector
import com.munzenberger.money.data.sql.NewFileSqlMoneyRepositoryConnector
import com.munzenberger.money.presentation.database.MoneyRepositoryConnectorFactory
import org.koin.dsl.module
import java.io.File

val dataModule = module {
    single<MoneyRepositoryConnectorFactory> {
        object : MoneyRepositoryConnectorFactory {
            override fun create(file: File) = NewFileSqlMoneyRepositoryConnector(file)
            override fun open(file: File) = ExistingFileSqlMoneyRepositoryConnector(file)
            override fun createInMemory() = InMemorySqlMoneyRepositoryConnector()
        }
    }
}
