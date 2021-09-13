package com.munzenberger.money.app.property

import com.munzenberger.money.app.SchedulerProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty

interface ReadOnlyAsyncObjectProperty<T> : ReadOnlyProperty<AsyncObject<T>>

interface ReadOnlyAsyncStatusProperty : ReadOnlyAsyncObjectProperty<Unit>

interface AsyncObjectProperty<T> : Property<AsyncObject<T>>, ReadOnlyAsyncObjectProperty<T>

interface AsyncStatusProperty : AsyncObjectProperty<Unit>, ReadOnlyAsyncStatusProperty

fun <T> AsyncObjectProperty<T>.asyncValue(
        single: Single<T>,
        subscribeOn: Scheduler = SchedulerProvider.database,
        observeOn: Scheduler = SchedulerProvider.main
): Disposable = single.subscribeOn(subscribeOn)
        .observeOn(observeOn)
        .doOnSubscribe { value = AsyncObject.Executing() }
        .subscribe(
                { value = AsyncObject.Complete(it) },
                { value = AsyncObject.Error(it) }
        )

fun <T> AsyncObjectProperty<T>.singleValue(
        block: () -> T
): Single<T> = Single.fromCallable(block)
        .doOnSubscribe { value = AsyncObject.Executing() }
        .doOnSuccess { value = AsyncObject.Complete(it) }
        .doOnError { value = AsyncObject.Error(it) }
