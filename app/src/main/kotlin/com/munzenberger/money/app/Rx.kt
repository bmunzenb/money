package com.munzenberger.money.app

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> Single<T>.useDatabaseSchedulers() = this
        //.delay(2, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.single())
        .observeOn(JavaFxScheduler.platform())

fun Completable.useDatabaseSchedulers() = this
        //.delay(1, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.single())
        .observeOn(JavaFxScheduler.platform())
