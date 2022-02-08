package com.munzenberger.money.app.property

import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty

interface ReadOnlyAsyncObjectProperty<T> : ReadOnlyProperty<AsyncObject<T>>

@Deprecated("Replace with callback.")
interface ReadOnlyAsyncStatusProperty : ReadOnlyAsyncObjectProperty<Unit>

interface AsyncObjectProperty<T> : Property<AsyncObject<T>>, ReadOnlyAsyncObjectProperty<T>

@Deprecated("Replace with callback.")
interface AsyncStatusProperty : AsyncObjectProperty<Unit>, ReadOnlyAsyncStatusProperty
