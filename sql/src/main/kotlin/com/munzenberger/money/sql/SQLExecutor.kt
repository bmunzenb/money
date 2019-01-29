package com.munzenberger.money.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.util.logging.Level
import java.util.logging.Logger

object SQLExecutor {

    private val logger = Logger.getLogger(SQLExecutor::class.java.name)

    fun execute(connection: Connection, sql: String, parameters: List<Any> = emptyList()) {
        logger.log(Level.FINE, "execute: $sql -> ${parameters.toParamString()}")

        connection.prepareStatement(sql).use {
            it.setParameters(parameters)
            it.execute()
        }
    }

    fun executeQuery(connection: Connection, sql: String, parameters: List<Any> = emptyList(), handler: ResultSetHandler? = null) {
        logger.log(Level.FINE, "executeQuery: $sql -> ${parameters.toParamString()}")

        connection.prepareStatement(sql).use {
            it.setParameters(parameters)
            val resultSet = it.executeQuery()
            handler?.onResultSet(resultSet)
        }
    }

    fun executeUpdate(connection: Connection, sql: String, parameters: List<Any> = emptyList(), handler: ResultSetHandler? = null): Int {
        logger.log(Level.FINE, "executeUpdate: $sql -> ${parameters.toParamString()}")

        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use {
            it.setParameters(parameters)
            val updated = it.executeUpdate()
            handler?.run {
                onResultSet(it.generatedKeys)
            }
            updated
        }
    }

    private fun PreparedStatement.setParameters(parameters: List<Any>) {
        parameters.withIndex().forEach {
            setObject(it.index + 1, it.value)
        }
    }

    private fun List<Any>.toParamString() = joinToString {
        when (it) {
            is String -> "\"$it\""
            else -> it.toString()
        }
    }
}
