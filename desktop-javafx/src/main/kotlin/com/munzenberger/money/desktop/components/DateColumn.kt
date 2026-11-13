package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.strings
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/**
 * A column titled by the string for [titleKey], identified by [id], showing each row's [value] as the locale's numeric
 * date. It holds the dates themselves, so it sorts them by date rather than by their text.
 */
internal fun <S> dateColumn(id: String, titleKey: String, value: (S) -> LocalDate) =
    TableColumn<S, LocalDate>(strings.getString(titleKey)).apply {
        this.id = id
        setCellValueFactory { features -> ReadOnlyObjectWrapper(features.value?.let(value)) }
        setCellFactory { DateCell() }
    }

private class DateCell<S> : TableCell<S, LocalDate>() {
    override fun updateItem(item: LocalDate?, empty: Boolean) {
        super.updateItem(item, empty)
        text = if (empty || item == null) null else DATE_FORMATTER.format(item.toJavaLocalDate())
    }
}
