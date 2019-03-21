package com.munzenberger.money.app.property

import com.munzenberger.money.app.useDatabaseSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import javafx.beans.property.SimpleObjectProperty

open class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), ReadOnlyAsyncObjectProperty<T> {

    fun subscribe(single: Single<T>) {

        set(AsyncObject.Executing())

        single.useDatabaseSchedulers()
                .subscribe({ set(AsyncObject.Complete(it)) }, { set(AsyncObject.Error(it)) })
    }
}

class SimpleAsyncStatusProperty(value: AsyncObject<Unit> = AsyncObject.Pending())
    : SimpleAsyncObjectProperty<Unit>(value), ReadOnlyAsyncStatusProperty {

    fun subscribe(completable: Completable) {

        set(AsyncObject.Executing())

        completable.useDatabaseSchedulers()
                .subscribe({ set(AsyncObject.Complete(Unit)) }, { set(AsyncObject.Error(it)) })
    }
}
