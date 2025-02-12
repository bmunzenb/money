package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class QueryViewModel {
    data class QueryResult(
        val columns: MutableList<String> = mutableListOf(),
        val data: ObservableList<List<Any?>> = FXCollections.observableArrayList(),
        var message: String? = null,
    )

    private val result = SimpleAsyncObjectProperty<QueryResult>()

    val queryProperty = SimpleStringProperty()
    val selectedQueryProperty = SimpleStringProperty()
    val resultProperty: ReadOnlyAsyncObjectProperty<QueryResult> = result

    private lateinit var database: MoneyDatabase

    fun start(database: MoneyDatabase) {
        this.database = database
    }

    private val queryString: String?
        get() = listOf(selectedQueryProperty.value, queryProperty.value).firstOrNull { it.isNotBlank() }

    fun executeQuery() {
        queryString?.run { result.setValueAsync { executeQuery(this) } }
    }

    fun executeUpdate() {
        queryString?.run { result.setValueAsync { executeUpdate(this) } }
    }

    private fun executeQuery(input: String): QueryResult {
        val result = QueryResult()

        database.executeQuery(Query(input)) { resultSet ->

            val md = resultSet.metaData
            val colCount = md.columnCount

            for (i in 1..colCount) {
                result.columns.add(md.getColumnLabel(i))
            }

            while (resultSet.next()) {
                val row = mutableListOf<Any?>()
                for (i in 1..colCount) {
                    row.add(resultSet.getObject(i))
                }
                result.data.add(row)
            }
        }

        return result.apply {
            message = "${result.data.size} row(s) returned."
        }
    }

    private fun executeUpdate(input: String): QueryResult {
        val updated = database.executeUpdate(Query(input))

        return QueryResult(message = "$updated row(s) updated.")
    }
}
