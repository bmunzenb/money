package com.munzenberger.money.app

import io.reactivex.Scheduler
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers

object SchedulerProvider {
    val database: Scheduler = Schedulers.single()
    val main: Scheduler = JavaFxScheduler.platform()
}
