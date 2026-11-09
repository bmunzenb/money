package com.munzenberger.money.core.register

/** Which dates of register entries [GetAccountRegisterUseCase] returns, relative to today. */
enum class RegisterDateFilter {
    AllDates,

    /** Entries in today's calendar month, including later dates in the month. */
    CurrentMonth,

    /** Entries in today's calendar year, including later dates in the year. */
    CurrentYear,

    /** Entries from the same day 3 months ago on, including later dates. */
    Last3Months,

    /** Entries from the same day 12 months ago on, including later dates. */
    Last12Months,
}
