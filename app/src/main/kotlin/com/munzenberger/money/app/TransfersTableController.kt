package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.PendingCategory
import com.munzenberger.money.core.Money
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import java.net.URL

class TransfersTableController {

    companion object {
        val LAYOUT: URL = TransfersTableController::class.java.getResource("TransfersTableLayout.fxml")
    }

    @FXML private lateinit var tableView: TableView<EditTransfer>
    @FXML private lateinit var categoryColumn: TableColumn<EditTransfer, DelayedCategory>
    @FXML private lateinit var memoColumn: TableColumn<EditTransfer, String>
    @FXML private lateinit var amountColumn: TableColumn<EditTransfer, Money>

    fun start(transfers: List<EditTransfer>, categories: List<DelayedCategory>) {

        tableView.apply {
            items = FXCollections.observableArrayList<EditTransfer>(transfers)
        }

        categoryColumn.apply {
            val converter = BlockStringConverter<DelayedCategory>(DelayedCategory::name) { PendingCategory(it) }
            cellFactory = ComboBoxTableCell.forTableColumn(converter, FXCollections.observableList(categories))
            cellValueFactory = Callback { t -> t.value.selectedCategoryProperty }
        }

        memoColumn.apply {
            cellFactory = TextFieldTableCell.forTableColumn()
            cellValueFactory = Callback { t -> t.value.memoProperty }
        }

        amountColumn.apply {
            cellFactory = TextFieldTableCell.forTableColumn(MoneyStringConverter())
            cellValueFactory = Callback { t -> t.value.amountProperty }
        }
    }
}
