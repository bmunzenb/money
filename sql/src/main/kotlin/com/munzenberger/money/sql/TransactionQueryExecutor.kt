package com.munzenberger.money.sql

interface TransactionQueryExecutor : QueryExecutor {

    fun commit()

    fun rollback()
}
