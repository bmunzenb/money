package com.munzenberger.money.sql

import java.sql.ResultSet
import java.util.function.Consumer
import java.util.function.Function

@FunctionalInterface
interface ResultSetHandler : Consumer<ResultSet>

@FunctionalInterface
interface ResultSetMapper<T> : Function<ResultSet, T>

open class ListResultSetHandler<T>(private val mapper: ResultSetMapper<T>) : ResultSetHandler {

    private val mutableResults = mutableListOf<T>()

    val results: List<T>
        get() = mutableResults

    override fun accept(resultSet: ResultSet) {

        while (resultSet.next()) {
            mutableResults.add(mapper.apply(resultSet))
        }
    }
}

open class FirstResultSetHandler<T>(private val mapper: ResultSetMapper<T>) : ResultSetHandler {

    private var mutableResult: T? = null

    val result: T?
        get() = mutableResult

    override fun accept(resultSet: ResultSet) {

        if (resultSet.next()) {
            mutableResult = mapper.apply(resultSet)
        }
    }
}
