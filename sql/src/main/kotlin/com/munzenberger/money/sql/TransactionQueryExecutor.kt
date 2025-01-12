package com.munzenberger.money.sql

interface TransactionQueryExecutor : QueryExecutor {
    fun commit()

    fun rollback()

    fun addRollbackListener(listener: Runnable)
}
