package com.munzenberger.money.app.model

import java.time.LocalDate
import java.util.function.Predicate

class FXRegisterEntryFilter(
        val name: String,
        predicate: Predicate<FXRegisterEntry>
) : Predicate<FXRegisterEntry> by predicate {
    override fun toString() = name
}

fun LocalDate.inCurrentMonth(): Boolean =
        LocalDate.now().let { month == it.month && year == it.year }

fun LocalDate.inCurrentYear(): Boolean =
        LocalDate.now().year == year

fun LocalDate.inLastMonths(months: Long): Boolean =
        isAfter(LocalDate.now().minusMonths(months))
