package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.sql.ResultSetHandler
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.sql.ResultSet

class QueryViewModel {

    data class QueryResult(
            val columns: MutableList<String> = mutableListOf(),
            val data: ObservableList<List<Any>> = FXCollections.observableArrayList(),
            var message: String? = null
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
        queryString?.run {
            subscribe(observableQuery(this))
        }
    }

    fun executeUpdate() {
        queryString?.run {
            subscribe(observableUpdate(this))
        }
    }

    private fun subscribe(single: Single<QueryResult>) {

        result.set(AsyncObject.Executing())

        single.useDatabaseSchedulers()
                .subscribe({ result.set(AsyncObject.Complete(it)) }, { result.set(AsyncObject.Error(it)) })
    }

    private fun observableQuery(input: String) = Single.fromCallable {

        val result = QueryResult()

        database.executeQuery(Query(input), object : ResultSetHandler {
            override fun onResultSet(resultSet: ResultSet) {

                val md = resultSet.metaData
                val colCount = md.columnCount

                for (i in 1..colCount) {
                    result.columns.add(md.getColumnLabel(i))
                }

                while (resultSet.next()) {
                    val row = mutableListOf<Any>()
                    for (i in 1..colCount) {
                        row.add(resultSet.getObject(i))
                    }
                    result.data.add(row)
                }
            }
        })

        result.apply {
            message = "${result.data.size} row(s) returned."
        }
    }

    private fun observableUpdate(input: String) = Single.fromCallable {

        val updated = database.executeUpdate(Query(input))

        QueryResult(message = "$updated row(s) updated.")
    }
}
