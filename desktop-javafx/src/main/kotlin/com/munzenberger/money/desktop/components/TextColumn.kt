package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.strings
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.scene.control.TableColumn

/** A column titled by the string for [titleKey], identified by [id], showing each row's [value]. */
internal fun <S> textColumn(id: String, titleKey: String, value: (S) -> String) =
    TableColumn<S, String>(strings.getString(titleKey)).apply {
        this.id = id
        setCellValueFactory { features -> ReadOnlyStringWrapper(features.value?.let(value).orEmpty()) }
    }
