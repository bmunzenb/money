package com.munzenberger.money.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.logging.Level
import java.util.logging.Logger

object SQLExecutor {

    private val logger = Logger.getLogger(SQLExecutor::class.java.name)

    fun executeQuery(connection: Connection, sql: String, parameters: List<Any>? = null, handler: ResultSetHandler? = null) {
        logger.log(Level.FINE, "executeQuery: $sql -> $parameters")

        connection.prepareStatement(sql).use { preparedStatement ->

            preparedStatement.setParameters(parameters)

            preparedStatement.executeQuery().use { resultSet ->
                handler?.onResultSet(resultSet)
            }
        }
    }

    fun executeUpdate(connection: Connection, sql: String, parameters: List<Any>? = null, handler: ResultSetHandler? = null): Int {
        logger.log(Level.FINE, "executeUpdate: $sql -> $parameters")

        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { preparedStatement ->

            preparedStatement.setParameters(parameters)

            val updated = preparedStatement.executeUpdate()

            preparedStatement.generatedKeys.use { generatedKeys ->
                handler?.onResultSet(generatedKeys)
            }

            updated
        }
    }

    private fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {

        try {
            return block(this)
        } finally {
            try {
                this.close()
            } catch (e: Exception) {
                logger.log(Level.WARNING, "failed to close resource", e)
            }
        }
    }

    private fun PreparedStatement.setParameters(parameters: List<Any>?) {

        parameters?.withIndex()?.forEach {
            setObject(it.index + 1, it.value)
        }
    }
}
