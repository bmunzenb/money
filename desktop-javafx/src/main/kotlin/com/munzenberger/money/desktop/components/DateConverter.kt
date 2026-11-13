package com.munzenberger.money.desktop.components

import javafx.util.StringConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Converts a date picker's dates to and from the locale's numeric date, as [dateColumn] shows them. Text that isn't a
 * date converts to null.
 */
internal object DateConverter : StringConverter<LocalDate>() {
    override fun toString(date: LocalDate?): String = date?.let(DATE_FORMATTER::format).orEmpty()

    override fun fromString(text: String?): LocalDate? =
        try {
            text?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it.trim(), DATE_FORMATTER) }
        } catch (_: DateTimeParseException) {
            null
        }
}

// The locale's numeric date, with a four-digit year, e.g. 1/15/2026 in the US.
internal val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedPattern("yMd")
