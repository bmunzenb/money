package com.munzenberger.money.desktop

import com.munzenberger.money.presentation.inject.presentationModule
import javafx.application.Application
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(presentationModule)
    }

    Application.launch(MoneyApplication::class.java, *args)
}
