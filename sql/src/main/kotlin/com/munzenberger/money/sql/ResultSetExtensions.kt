package com.munzenberger.money.sql

import java.sql.ResultSet
import java.time.LocalDate

fun ResultSet.getLongOrNull(columnLabel: String) =
        getObject(columnLabel)?.let {
            when (it) {
                is Number -> it.toLong()
                else -> null
            }
        }

fun ResultSet.getLocalDate(columnLabel: String): LocalDate =
        getLong(columnLabel).let { LocalDate.ofEpochDay(it) }

fun ResultSet.getLocalDateOrNull(columnLabel: String): LocalDate? =
        getObject(columnLabel)?.let {
            when (it) {
                is Number -> LocalDate.ofEpochDay(it.toLong())
                else -> null
            }
        }
