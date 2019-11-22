package com.munzenberger.money.sql

interface QueryExecutor {

    fun execute(query: Query): Boolean

    fun executeQuery(query: Query, handler: ResultSetHandler? = null)

    fun executeUpdate(query: Query, handler: ResultSetHandler? = null): Int

    fun createTransaction(): TransactionQueryExecutor

    fun <T> getList(query: Query, mapper: ResultSetMapper<T>): List<T> {
        return ListResultSetHandler(mapper).let {
            executeQuery(query, it)
            it.results
        }
    }

    fun <T> getFirst(query: Query, mapper: ResultSetMapper<T>): T? {
        return FirstResultSetHandler(mapper).let {
            executeQuery(query, it)
            it.result
        }
    }
}

fun QueryExecutor.doInTransaction(block: (tx: TransactionQueryExecutor) -> Unit) {
    val tx = createTransaction()
    try {
        block.invoke(tx)
        tx.commit()
    } catch (e: Throwable) {
        tx.rollback()
        throw e
    }
}
