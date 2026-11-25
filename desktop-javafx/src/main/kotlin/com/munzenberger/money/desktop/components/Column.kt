package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.strings
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn

/** A column titled by the string for [titleKey], identified by [id], whose cells are each given their row's [value]. */
internal fun <S, T> column(id: String, titleKey: String, value: (S) -> ObservableValue<T>) =
    TableColumn<S, T>(strings.getString(titleKey)).apply {
        this.id = id
        setCellValueFactory { features -> features.value?.let(value) }
    }
