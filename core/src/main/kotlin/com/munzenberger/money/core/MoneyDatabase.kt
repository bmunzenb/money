package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

interface MoneyDatabase : QueryExecutor {

    val name: String

    val dialect: DatabaseDialect

    fun close()
}

abstract class AbstractMoneyDatabase(
        override val name: String,
        override val dialect: DatabaseDialect,
        executor: QueryExecutor
) : MoneyDatabase, QueryExecutor by executor
