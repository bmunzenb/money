package com.munzenberger.money.app.property

import io.reactivex.rxjava3.core.Single
import javafx.application.Platform

@Deprecated("Replace with something other than Single.")
fun <T : Any> Single<T>.bindProperty(property: AsyncObjectProperty<T>): Single<T> {
    return this
            .doOnSubscribe {
                Platform.runLater { property.value = AsyncObject.Executing() }
            }
            .doOnSuccess {
                Platform.runLater { property.value = AsyncObject.Complete(it) }
            }
            .doOnError {
                Platform.runLater { property.value = AsyncObject.Error(it) }
            }
}
