package com.munzenberger.money.app

import io.reactivex.Scheduler
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers

interface SchedulerProvider {

    val single: Scheduler
    val main: Scheduler

    companion object {
        val Default = object : SchedulerProvider {
            override val single = Schedulers.single()
            override val main = JavaFxScheduler.platform()
        }
    }
}
