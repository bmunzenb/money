package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncValue
import javafx.fxml.FXML
import javafx.scene.control.Hyperlink
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.Callback
import java.net.URL

class CategoryListController : AutoCloseable {

    companion object {
        val LAYOUT: URL = CategoryListController::class.java.getResource("CategoryListLayout.fxml")
    }

    @FXML lateinit var treeTableView: TreeTableView<FXCategory>
    @FXML lateinit var nameColumn: TreeTableColumn<FXCategory, String>
    @FXML lateinit var typeColumn: TreeTableColumn<FXCategory, String>

    private val viewModel = CategoryListViewModel()

    fun initialize() {

        treeTableView.rootProperty().bindAsyncValue(viewModel.categoriesProperty) { it }

        treeTableView.placeholderProperty().bindAsync(viewModel.categoriesProperty) { async ->
            when (async) {

                is AsyncObject.Pending, is AsyncObject.Executing -> ProgressIndicator().apply {
                    setPrefSize(60.0, 60.0)
                    setMaxSize(60.0, 60.0)
                }

                is AsyncObject.Complete -> Text("No categories.")

                is AsyncObject.Error -> Hyperlink(async.error.message).apply {
                    textFill = Color.RED
                    setOnAction { ErrorAlert(async.error).showAndWait() }
                }
            }
        }

        nameColumn.apply {
            cellValueFactory = Callback { it.value.value.nameProperty }
        }

        typeColumn.apply {
            cellValueFactory = Callback { it.value.value.typeProperty }
        }
    }

    fun start(database: ObservableMoneyDatabase) {
        viewModel.start(database)
    }

    override fun close() {
        viewModel.close()
    }
}
