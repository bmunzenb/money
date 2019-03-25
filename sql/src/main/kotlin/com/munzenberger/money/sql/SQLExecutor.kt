package com.munzenberger.money.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.logging.Level
import java.util.logging.Logger

object SQLExecutor {

    private val logger = Logger.getLogger(SQLExecutor::class.java.name)

    fun execute(connection: Connection, sql: String, parameters: List<Any?> = emptyList()): Boolean {
        logger.log(Level.FINE, toString(sql, parameters))

        return connection.prepareStatement(sql).use {
            it.setParameters(parameters)
            it.execute()
        }
    }

    fun executeQuery(connection: Connection, sql: String, parameters: List<Any?> = emptyList(), handler: ResultSetHandler? = null) {
        logger.log(Level.FINE, toString(sql, parameters))

        connection.prepareStatement(sql).use {
            it.setParameters(parameters)
            val resultSet = it.executeQuery()
            handler?.accept(resultSet)
        }
    }

    fun executeUpdate(connection: Connection, sql: String, parameters: List<Any?> = emptyList(), handler: ResultSetHandler? = null): Int {
        logger.log(Level.FINE, toString(sql, parameters))

        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use {
            it.setParameters(parameters)
            val updated = it.executeUpdate()
            handler?.run {
                accept(it.generatedKeys)
            }
            updated
        }
    }

    private fun PreparedStatement.setParameters(parameters: List<Any?>) {
        parameters.withIndex().forEach {
            setObject(it.index + 1, it.value)
        }
    }

    private fun toString(sql: String, parameters: List<Any?>) = when {
            parameters.isEmpty() -> sql
            else -> "$sql <<= ${parameters.joinToString(prefix = "[", postfix = "]") {
                when (it) {
                    is String -> "\"$it\""
                    else -> it.toString()
                }
            }}"
        }
}
