package com.munzenberger.money.app

import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.text.Text
import javafx.util.Callback
import java.net.URL

class CategoryListController : AutoCloseable {
    companion object {
        val LAYOUT: URL = CategoryListController::class.java.getResource("CategoryListLayout.fxml")
    }

    @FXML lateinit var tableView: TableView<FXCategory>

    @FXML lateinit var nameColumn: TableColumn<FXCategory, String>

    @FXML lateinit var typeColumn: TableColumn<FXCategory, String>

    private val viewModel = CategoryListViewModel()

    fun initialize() {
        tableView.bindAsync(
            listProperty = viewModel.categoriesProperty,
            placeholder = Text("No categories."),
        )

        nameColumn.apply {
            cellValueFactory = Callback { it.value.nameProperty }
        }

        typeColumn.apply {
            cellValueFactory = Callback { it.value.typeProperty }
        }
    }

    fun start(database: ObservableMoneyDatabase) {
        viewModel.start(database)
    }

    override fun close() {
        viewModel.close()
    }
}
