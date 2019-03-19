package com.munzenberger.money.app.property

import com.munzenberger.money.app.useDatabaseSchedulers
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty

class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), ReadOnlyAsyncObjectProperty<T> {

    fun subscribe(single: Single<T>) {

        set(AsyncObject.Executing())

        single.useDatabaseSchedulers()
                .subscribe({ set(AsyncObject.Complete(it)) }, { set(AsyncObject.Error(it)) })
    }
}
