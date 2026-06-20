package com.munzenberger.money.repository.api

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

fun LocalDate.Companion.today(tz: TimeZone = TimeZone.currentSystemDefault()) = Clock.System.todayIn(tz)
