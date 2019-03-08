package com.munzenberger.money.core

import com.munzenberger.money.sql.QueryExecutor

abstract class MoneyDatabase(
        val name: String,
        val dialect: DatabaseDialect,
        executor: QueryExecutor
) : QueryExecutor by executor {

    abstract fun close()
}
