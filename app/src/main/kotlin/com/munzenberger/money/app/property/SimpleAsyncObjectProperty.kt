package com.munzenberger.money.app.property

import com.munzenberger.money.app.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javafx.beans.property.SimpleObjectProperty

open class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), AsyncObjectProperty<T> {

    override fun subscribeTo(single: Single<T>, schedulers: SchedulerProvider): Disposable {

        set(AsyncObject.Executing())

        return single
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .subscribe({ set(AsyncObject.Complete(it)) }, { set(AsyncObject.Error(it)) })
    }
}

class SimpleAsyncStatusProperty(value: AsyncObject<Unit> = AsyncObject.Pending())
    : SimpleAsyncObjectProperty<Unit>(value), AsyncStatusProperty {

    override fun subscribeTo(completable: Completable, schedulers: SchedulerProvider): Disposable {

        set(AsyncObject.Executing())

        return completable
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .subscribe({ set(AsyncObject.Complete(Unit)) }, { set(AsyncObject.Error(it)) })
    }
}
