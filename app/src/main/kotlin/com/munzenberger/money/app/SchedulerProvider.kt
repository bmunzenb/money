package com.munzenberger.money.app

import io.reactivex.Scheduler
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

interface SchedulerProvider {

    val database: Scheduler
    val main: Scheduler

    companion object {
        val Default = object : SchedulerProvider {

            private val databaseExecutor = Executors.newSingleThreadExecutor()

            override val database = Schedulers.from(databaseExecutor)
            override val main = JavaFxScheduler.platform()
        }
    }
}
