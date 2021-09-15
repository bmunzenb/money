package com.munzenberger.money.app.concurrent

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler

object Schedulers {
    val SINGLE: Scheduler = Schedulers.from(Executors.SINGLE)
    val PLATFORM: Scheduler = JavaFxScheduler.platform()
}
