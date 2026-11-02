package com.munzenberger.money.desktop

import com.munzenberger.money.data.api.MoneyRepositoryConnectorFactory
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnectorFactory
import com.munzenberger.money.presentation.inject.presentationModule
import javafx.application.Application
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val dataModule = module {
    single<MoneyRepositoryConnectorFactory> { SqlMoneyRepositoryConnectorFactory() }
}

fun main(args: Array<String>) {
    startKoin {
        modules(presentationModule, dataModule)
    }

    Application.launch(MoneyApplication::class.java, *args)
}
