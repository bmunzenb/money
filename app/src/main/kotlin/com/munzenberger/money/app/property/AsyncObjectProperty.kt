package com.munzenberger.money.app.property

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty

interface ReadOnlyAsyncObjectProperty<T> : ReadOnlyProperty<AsyncObject<T>>

interface ReadOnlyAsyncStatusProperty : ReadOnlyAsyncObjectProperty<Unit>

interface AsyncObjectProperty<T> : Property<AsyncObject<T>>, ReadOnlyAsyncObjectProperty<T> {
    fun subscribe(single: Single<T>): Disposable
}

interface AsyncStatusProperty : AsyncObjectProperty<Unit>, ReadOnlyAsyncStatusProperty {
    fun subscribe(completable: Completable): Disposable
}
