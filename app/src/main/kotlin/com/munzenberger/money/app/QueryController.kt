package com.munzenberger.money.app

import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.MoneyDatabase
import javafx.fxml.FXML
import javafx.scene.Node
import java.net.URL
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import java.util.logging.Level
import java.util.logging.Logger


class QueryController {

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

        queryTextArea.disableProperty().bindAsync(viewModel.resultProperty, AsyncObject.Status.EXECUTING)
        queryButton.disableProperty().bindAsync(viewModel.resultProperty, AsyncObject.Status.EXECUTING)
        updateButton.disableProperty().bindAsync(viewModel.resultProperty, AsyncObject.Status.EXECUTING)

        viewModel.queryProperty.bind(queryTextArea.textProperty())
        viewModel.selectedQueryProperty.bind(queryTextArea.selectedTextProperty())

        viewModel.resultProperty.addListener { _, _, value ->
            when (value) {
                is AsyncObject.Executing -> onExecuting()
                is AsyncObject.Complete -> onResult(value.value)
                is AsyncObject.Error -> onError(value.error)
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
            placeholder = ProgressIndicator().apply {
                minWidth = 60.0
                minHeight = 60.0
            }
        }
    }

    private fun onResult(result: QueryViewModel.QueryResult) {

        resultTableView.apply {
            columns.clear()
            items = result.data
            result.columns.forEachIndexed { index, s ->
                val col = TableColumn<List<Any?>, Any>().apply {
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
            placeholder = Label(error.message).apply {
                textFill = Color.RED
            }
        }

        logger.log(Level.WARNING, "query failure", error)
    }
}
