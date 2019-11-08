package com.munzenberger.money.app.model

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun LocalDate.toDate() = Date.from(atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())