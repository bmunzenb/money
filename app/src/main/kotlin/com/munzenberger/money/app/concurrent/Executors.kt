package com.munzenberger.money.app.concurrent

import javafx.application.Platform
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Executors {

    private val singleService: ExecutorService = Executors.newSingleThreadExecutor()
    val SINGLE: Executor = singleService

    val PLATFORM: Executor = Executor { command -> Platform.runLater(command) }

    fun shutdown() {
        singleService.shutdown()
    }
}
