package com.munzenberger.money.app.property

import io.reactivex.Observable

fun <T, R> Observable<T>.flatMapAsyncObject(block: (T) -> R): Observable<AsyncObject<R>> =
        flatMap { input ->
            Observable.create<AsyncObject<R>> {
                it.onNext(AsyncObject.Executing())
                val output = block.invoke(input)
                it.onNext(AsyncObject.Complete(output))
            }.onErrorReturn { AsyncObject.Error(it) }
        }
