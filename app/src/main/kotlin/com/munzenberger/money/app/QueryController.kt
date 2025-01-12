package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.paint.Color
import javafx.scene.text.Text
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger

class QueryController : AutoCloseable {
    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("QueryLayout.fxml")
    }

    private val logger = Logger.getLogger(QueryController::class.java.name)

    @FXML lateinit var container: Node

    @FXML lateinit var queryTextArea: TextArea

    @FXML lateinit var queryButton: Button

    @FXML lateinit var updateButton: Button

    @FXML lateinit var resultTableView: TableView<List<Any?>>

    private val viewModel = QueryViewModel()

    fun initialize() {
        resultTableView.placeholder = Text("Execute a statement to see results.")

        queryTextArea.disableProperty().bindAsyncStatus(viewModel.resultProperty, AsyncObject.Status.EXECUTING)
        queryButton.disableProperty().bindAsyncStatus(viewModel.resultProperty, AsyncObject.Status.EXECUTING)
        updateButton.disableProperty().bindAsyncStatus(viewModel.resultProperty, AsyncObject.Status.EXECUTING)

        viewModel.queryProperty.bind(queryTextArea.textProperty())
        viewModel.selectedQueryProperty.bind(queryTextArea.selectedTextProperty())

        viewModel.resultProperty.addListener { _, _, value ->
            when (value) {
                is AsyncObject.Executing -> onExecuting()
                is AsyncObject.Complete -> onResult(value.value)
                is AsyncObject.Error -> onError(value.error)
                else -> {}
            }
        }
    }

    fun start(database: MoneyDatabase) {
        viewModel.start(database)
    }

    @FXML fun onQueryButton() {
        viewModel.executeQuery()
    }

    @FXML fun onUpdateButton() {
        viewModel.executeUpdate()
    }

    private fun onExecuting() {
        resultTableView.apply {
            columns.clear()
            items = null
            placeholder =
                ProgressIndicator().apply {
                    setPrefSize(60.0, 60.0)
                    setMaxSize(60.0, 60.0)
                }
        }
    }

    private fun onResult(result: QueryViewModel.QueryResult) {
        resultTableView.apply {
            columns.clear()
            items = result.data
            result.columns.forEachIndexed { index, s ->
                val col =
                    TableColumn<List<Any?>, Any>().apply {
                        text = s
                        setCellValueFactory { ReadOnlyObjectWrapper(it.value[index]) }
                    }
                columns.add(col)
            }
            placeholder = Text(result.message)
        }
    }

    private fun onError(error: Throwable) {
        resultTableView.apply {
            columns.clear()
            items = null
            placeholder =
                Label(error.message).apply {
                    textFill = Color.RED
                }
        }

        logger.log(Level.WARNING, "query failure", error)
    }

    override fun close() {
        // nothing to close
    }
}
