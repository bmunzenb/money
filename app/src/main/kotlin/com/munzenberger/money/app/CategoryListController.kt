package com.munzenberger.money.app

import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXCategory
import javafx.fxml.FXML
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView
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

        treeTableView.rootProperty().bind(viewModel.rootProperty)

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
