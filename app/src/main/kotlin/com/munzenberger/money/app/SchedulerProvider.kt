package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler

object SchedulerProvider {
    val SINGLE: Scheduler = Schedulers.from(Executors.SINGLE)
    val PLATFORM: Scheduler = JavaFxScheduler.platform()
}
