package com.munzenberger.money.app.property

import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty

interface ReadOnlyAsyncObjectProperty<T> : ReadOnlyProperty<AsyncObject<T>>

interface AsyncObjectProperty<T> : Property<AsyncObject<T>>, ReadOnlyAsyncObjectProperty<T>
