package com.munzenberger.money.app.property

import io.reactivex.rxjava3.core.Single

fun <T : Any> Single<T>.bindProperty(property: AsyncObjectProperty<T>): Single<T> =
        doOnSubscribe { property.value = AsyncObject.Executing() }
                .doOnSuccess { property.value = AsyncObject.Complete(it) }
                .doOnError { property.value = AsyncObject.Error(it) }
