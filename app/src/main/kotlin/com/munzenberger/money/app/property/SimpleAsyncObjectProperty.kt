package com.munzenberger.money.app.property

import com.munzenberger.money.app.useDatabaseSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javafx.beans.property.SimpleObjectProperty

open class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), AsyncObjectProperty<T> {

    override fun subscribe(single: Single<T>): Disposable {

        set(AsyncObject.Executing())

        return single.useDatabaseSchedulers()
                .subscribe({ set(AsyncObject.Complete(it)) }, { set(AsyncObject.Error(it)) })
    }
}

class SimpleAsyncStatusProperty(value: AsyncObject<Unit> = AsyncObject.Pending())
    : SimpleAsyncObjectProperty<Unit>(value), AsyncStatusProperty {

    override fun subscribe(completable: Completable): Disposable {

        set(AsyncObject.Executing())

        return completable.useDatabaseSchedulers()
                .subscribe({ set(AsyncObject.Complete(Unit)) }, { set(AsyncObject.Error(it)) })
    }
}
