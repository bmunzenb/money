package com.munzenberger.money.app.concurrent

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Executors {

    val SINGLE: ExecutorService = Executors.newSingleThreadExecutor()

    fun shutdown() {
        SINGLE.shutdown()
    }
}
