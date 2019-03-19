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


class QueryController {

    companion object {
        val LAYOUT: URL = NavigationController::class.java.getResource("QueryLayout.fxml")
    }

    @FXML lateinit var container: Node
    @FXML lateinit var queryTextArea: TextArea
    @FXML lateinit var queryButton: Button
    @FXML lateinit var updateButton: Button
    @FXML lateinit var resultTableView: TableView<List<Any>>

    private val viewModel = QueryViewModel()

    fun initialize() {

        container.disableProperty().bindAsync(viewModel.resultProperty, AsyncObject.Status.EXECUTING)

        viewModel.queryProperty.bind(queryTextArea.textProperty())
        viewModel.selectedQueryProperty.bind(queryTextArea.selectedTextProperty())

        viewModel.resultProperty.addListener { _, _, value ->
            when (value) {
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

    private fun onResult(result: QueryViewModel.QueryResult) {

        resultTableView.getColumns().clear()
        resultTableView.setItems(result.data)

        for (i in 0 until result.columns.size) {

            val col = TableColumn<List<Any>, Any>().apply {
                text = result.columns[i]
                setCellValueFactory { p -> ReadOnlyObjectWrapper(p.value[i]) }
            }

            resultTableView.columns.add(col)
        }

        resultTableView.placeholder = Text(result.message)
    }

    private fun onError(error: Throwable) {

        resultTableView.apply {
            columns.clear()
            items = null
            placeholder = Label(error.message).apply {
                textFill = Color.RED
            }
        }
    }
}
