package com.munzenberger.money.app.property

import com.munzenberger.money.app.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javafx.beans.property.SimpleObjectProperty
import java.util.logging.Level
import java.util.logging.Logger

open class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), AsyncObjectProperty<T> {

    private val logger = Logger.getLogger(SimpleAsyncObjectProperty::class.java.simpleName)

    override fun subscribeTo(single: Single<T>, schedulers: SchedulerProvider): Disposable {

        set(AsyncObject.Executing())

        return single
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.main)
                .doOnError { logger.log(Level.WARNING, it) { "Observer received error for AsyncObject" } }
                .subscribe({ set(AsyncObject.Complete(it)) }, { set(AsyncObject.Error(it)) })
    }
}

class SimpleAsyncStatusProperty(value: AsyncObject<Unit> = AsyncObject.Pending())
    : SimpleAsyncObjectProperty<Unit>(value), AsyncStatusProperty {

    private val logger = Logger.getLogger(SimpleAsyncStatusProperty::class.java.simpleName)

    override fun subscribeTo(completable: Completable, schedulers: SchedulerProvider): Disposable {

        set(AsyncObject.Executing())

        return completable
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.main)
                .doOnError { logger.log(Level.WARNING, it) { "Observer received error for AsyncObject" } }
                .subscribe({ set(AsyncObject.Complete(Unit)) }, { set(AsyncObject.Error(it)) })
    }
}
