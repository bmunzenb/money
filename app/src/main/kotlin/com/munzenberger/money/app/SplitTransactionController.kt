package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.model.TransactionCategory
import com.munzenberger.money.core.Money
import javafx.beans.binding.Bindings
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL

class SplitTransactionController {

    companion object {
        val LAYOUT: URL = SplitTransactionController::class.java.getResource("SplitTransactionLayout.fxml")
    }

    @FXML private lateinit var tableView: TableView<TransactionDetailEditor>
    @FXML private lateinit var categoryColumn: TableColumn<TransactionDetailEditor, TransactionCategory>
    @FXML private lateinit var memoColumn: TableColumn<TransactionDetailEditor, String>
    @FXML private lateinit var amountColumn: TableColumn<TransactionDetailEditor, Money>
    @FXML private lateinit var addButton: Button
    @FXML private lateinit var deleteButton: Button
    @FXML private lateinit var totalLabel: Label
    @FXML private lateinit var doneButton: Button

    private lateinit var stage: Stage

    private val viewModel = SplitTransactionViewModel()

    fun initialize() {

        tableView.apply {

            items = viewModel.editorsProperty

            selectionModel.selectionMode = SelectionMode.SINGLE

            placeholder = Text("Click the Add button to add transaction details.")
        }

        categoryColumn.apply {

            val converter = ListLookupStringConverter(
                    viewModel.categoriesProperty,
                    BlockStringConverter<TransactionCategory>(
                            toString = { c -> c.name },
                            toObject = { s -> TransactionCategory.Pending(s) }))

            cellFactory = Callback {
                ComboBoxTableCell<TransactionDetailEditor, TransactionCategory>(converter, viewModel.categoriesProperty).apply {
                    isComboBoxEditable = true
                }
            }

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

        deleteButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        { tableView.selectionModel.selectedItems.isEmpty() },
                        tableView.selectionModel.selectedItems
                )
        )

        doneButton.disableProperty().bind(viewModel.doneDisabledProperty)

        viewModel.totalProperty.addListener { _, _, newValue ->
            totalLabel.text = when (newValue) {
                null -> null
                else -> "Total: $newValue"
            }
        }
    }

    fun start(stage: Stage, transfers: ObservableList<TransactionDetailEditor>, categories: List<TransactionCategory>) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height

        viewModel.start(transfers, categories)
    }

    @FXML fun onAddButton() {
        val row = viewModel.add()
        tableView.edit(row, categoryColumn)
    }

    @FXML fun onDeleteButton() {
        viewModel.delete(tableView.selectionModel.selectedItems)
    }

    @FXML fun onDoneButton() {
        viewModel.done()
        stage.close()
    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
