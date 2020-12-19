package com.munzenberger.money.app

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler

object SchedulerProvider {
    val database: Scheduler = Schedulers.single()
    val main: Scheduler = JavaFxScheduler.platform()
}
