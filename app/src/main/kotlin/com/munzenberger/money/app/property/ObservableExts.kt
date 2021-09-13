package com.munzenberger.money.app.property

import io.reactivex.rxjava3.core.Observable

@Deprecated("Refactor to execute 'block' in subscribe.")
fun <T, R> Observable<T>.flatMapAsyncObject(block: (T) -> R): Observable<AsyncObject<R>> =
        flatMap { input: T ->
            Observable.create<AsyncObject<R>> {
                it.onNext(AsyncObject.Executing())
                val output = block.invoke(input)
                it.onNext(AsyncObject.Complete(output))
            }.onErrorReturn { AsyncObject.Error(it) }
        }
