package com.munzenberger.money.sql

import java.sql.ResultSet

interface ResultSetHandler {
    fun onResultSet(resultSet: ResultSet)
}

interface ResultSetMapper<out T> {
    fun map(resultSet: ResultSet): T
}

open class ListResultSetHandler<T>(private val mapper: ResultSetMapper<T>) : ResultSetHandler {

    private val mutableResults = mutableListOf<T>()

    val results: List<T>
        get() = mutableResults

    override fun onResultSet(resultSet: ResultSet) {

        while (resultSet.next()) {
            mutableResults.add(mapper.map(resultSet))
        }
    }
}

open class FirstResultSetHandler<T>(private val mapper: ResultSetMapper<T>) : ResultSetHandler {

    private var mutableResult: T? = null

    val result: T?
        get() = mutableResult

    override fun onResultSet(resultSet: ResultSet) {

        if (resultSet.next()) {
            mutableResult = mapper.map(resultSet)
        }
    }
}
