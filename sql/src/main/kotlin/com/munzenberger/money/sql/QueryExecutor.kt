package com.munzenberger.money.sql

interface QueryExecutor {
    fun execute(query: Query): Boolean

    fun executeQuery(
        query: Query,
        consumer: ResultSetConsumer,
    )

    fun executeUpdate(query: Query): Int

    fun createTransaction(): TransactionQueryExecutor

    fun <T> getList(
        query: Query,
        mapper: ResultSetMapper<T>,
    ): List<T> =
        ListResultSetConsumer(mapper).let {
            executeQuery(query, it)
            it.results
        }

    fun <T> getFirst(
        query: Query,
        mapper: ResultSetMapper<T>,
    ): T? =
        FirstResultSetConsumer(mapper).let {
            executeQuery(query, it)
            it.result
        }
}

fun QueryExecutor.transaction(block: (TransactionQueryExecutor) -> Unit) {
    createTransaction().run {
        try {
            block.invoke(this)
            commit()
        } catch (e: Throwable) {
            rollback()
            throw e
        }
    }
}
