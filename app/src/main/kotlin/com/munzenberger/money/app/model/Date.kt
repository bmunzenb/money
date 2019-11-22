package com.munzenberger.money.app.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun LocalDate.toDate(): Date =
        Date.from(atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

fun Date.toLocalDate(): LocalDate =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalDate()
