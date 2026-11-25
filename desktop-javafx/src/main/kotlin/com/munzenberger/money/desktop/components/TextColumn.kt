package com.munzenberger.money.desktop.components

import javafx.beans.property.ReadOnlyStringWrapper

/** A column titled by the string for [titleKey], identified by [id], showing each row's [value]. */
internal fun <S> textColumn(id: String, titleKey: String, value: (S) -> String) =
    column<S, String>(id, titleKey) { ReadOnlyStringWrapper(value(it)) }
