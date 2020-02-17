package com.munzenberger.money.app.property

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty

interface ReadOnlyAsyncObjectProperty<T> : ReadOnlyProperty<AsyncObject<T>>

interface ReadOnlyAsyncStatusProperty : ReadOnlyAsyncObjectProperty<Unit>

interface AsyncObjectProperty<T> : Property<AsyncObject<T>>, ReadOnlyAsyncObjectProperty<T>

interface AsyncStatusProperty : AsyncObjectProperty<Unit>, ReadOnlyAsyncStatusProperty

fun <T> Observable<T>.subscribe(property: AsyncObjectProperty<T>): Disposable =
        subscribe({ property.value = AsyncObject.Complete(it) }, { property.value = AsyncObject.Error(it) })

fun <T> Single<T>.subscribe(property: AsyncObjectProperty<T>): Disposable =
        subscribe({ property.value = AsyncObject.Complete(it) }, { property.value = AsyncObject.Error(it) })

fun Completable.subscribe(property: AsyncStatusProperty): Disposable =
        subscribe({ property.value = AsyncObject.Complete(Unit) }, { property.value = AsyncObject.Error(it) })
