package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler

object SchedulerProvider {
    val database: Scheduler = Schedulers.from(Executors.SINGLE)
    val main: Scheduler = JavaFxScheduler.platform()
}
