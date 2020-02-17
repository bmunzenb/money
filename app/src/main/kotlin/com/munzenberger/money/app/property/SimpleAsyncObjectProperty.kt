package com.munzenberger.money.app.property

import javafx.beans.property.SimpleObjectProperty

open class SimpleAsyncObjectProperty<T>(value: AsyncObject<T> = AsyncObject.Pending())
    : SimpleObjectProperty<AsyncObject<T>>(value), AsyncObjectProperty<T>

class SimpleAsyncStatusProperty(value: AsyncObject<Unit> = AsyncObject.Pending())
    : SimpleAsyncObjectProperty<Unit>(value), AsyncStatusProperty
