package com.munzenberger.money.app.property

import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty

class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), ReadOnlyAsyncObjectProperty<T> {

    fun subscribe(single: Single<T>) {

        set(AsyncObject.Executing())

        single.subscribeOn(Schedulers.single())
                .observeOn(JavaFxScheduler.platform())
                .subscribe({ set(AsyncObject.Complete(it)) }, { set(AsyncObject.Error(it)) })
    }
}
