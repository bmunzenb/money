package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

interface MoneyDatabase : QueryExecutor {

    val name: String

    val dialect: DatabaseDialect

    fun close()
}
