package com.munzenberger.money.sql

import java.sql.ResultSet

fun ResultSet.getLongOrNull(columnLabel: String) = getObject(columnLabel)?.let {
    when (it) {
        is Number -> it.toLong()
        else -> null
    }
}