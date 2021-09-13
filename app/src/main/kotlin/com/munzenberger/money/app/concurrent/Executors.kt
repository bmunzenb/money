package com.munzenberger.money.app.concurrent

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object Executors {
    val SINGLE: Executor = Executors.newSingleThreadExecutor {
        Executors.defaultThreadFactory().newThread(it).apply {
            isDaemon = true
        }
    }
}
