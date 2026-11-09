package com.munzenberger.money.desktop.register

import com.munzenberger.money.core.register.RegisterDateFilter
import com.munzenberger.money.core.register.RegisterStatusFilter
import com.munzenberger.money.desktop.strings
import javafx.util.StringConverter

/** Shows each date filter by its name, for a combo box that isn't editable. */
internal object RegisterDateFilterConverter : StringConverter<RegisterDateFilter>() {
    override fun toString(dateFilter: RegisterDateFilter?): String = dateFilter?.let {
        strings.getString(
            when (it) {
                RegisterDateFilter.AllDates -> "register_date_filter_all_dates"
                RegisterDateFilter.CurrentMonth -> "register_date_filter_current_month"
                RegisterDateFilter.CurrentYear -> "register_date_filter_current_year"
                RegisterDateFilter.Last3Months -> "register_date_filter_last_3_months"
                RegisterDateFilter.Last12Months -> "register_date_filter_last_12_months"
            }
        )
    }.orEmpty()

    // The combo box isn't editable, so it never converts text back to a date filter.
    override fun fromString(string: String?): RegisterDateFilter? = null
}

/** Shows each status filter by its name, for a combo box that isn't editable. */
internal object RegisterStatusFilterConverter : StringConverter<RegisterStatusFilter>() {
    override fun toString(statusFilter: RegisterStatusFilter?): String = statusFilter?.let {
        strings.getString(
            when (it) {
                RegisterStatusFilter.AllTransactions -> "register_status_filter_all_transactions"
                RegisterStatusFilter.UnreconciledTransactions -> "register_status_filter_unreconciled_transactions"
            }
        )
    }.orEmpty()

    // The combo box isn't editable, so it never converts text back to a status filter.
    override fun fromString(string: String?): RegisterStatusFilter? = null
}
