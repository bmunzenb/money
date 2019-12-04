package com.munzenberger.money.app

import com.munzenberger.money.app.control.BlockStringConverter
import com.munzenberger.money.app.control.ListLookupStringConverter
import com.munzenberger.money.app.control.MoneyStringConverter
import com.munzenberger.money.app.control.TextListCellFactory
import com.munzenberger.money.app.control.autoCompleteTextFormatter
import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.PendingCategory
import com.munzenberger.money.core.Money
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL

class EditTransfersController {

    companion object {
        val LAYOUT: URL = EditTransfersController::class.java.getResource("EditTransfersLayout.fxml")
    }

    @FXML private lateinit var tableView: TableView<EditTransfer>
    @FXML private lateinit var categoryColumn: TableColumn<EditTransfer, DelayedCategory>
    @FXML private lateinit var memoColumn: TableColumn<EditTransfer, String>
    @FXML private lateinit var amountColumn: TableColumn<EditTransfer, Money>
    @FXML private lateinit var categoryComboBox: ComboBox<DelayedCategory>
    @FXML private lateinit var memoTextField: TextField
    @FXML private lateinit var amountTextField: TextField
    @FXML private lateinit var addButton: Button
    @FXML private lateinit var doneButton: Button

    private lateinit var stage: Stage

    private val viewModel = EditTransfersViewModel()

    fun initialize() {

        tableView.apply {

            items = viewModel.transfersProperty

            selectionModel.selectionMode = SelectionMode.MULTIPLE

            setOnKeyReleased {
                when (it.code) {
                    KeyCode.DELETE -> viewModel.delete(selectionModel.selectedItems)
                    else -> { /* do nothing */ }
                }
            }

            placeholder = Text("Use the form below to add transaction details.")
        }

        categoryColumn.apply {

            val converter = ListLookupStringConverter(
                    viewModel.categoriesProperty,
                    BlockStringConverter<DelayedCategory>(
                            toString = { c -> c.name },
                            toObject = { s -> PendingCategory(s) }))

            cellFactory = Callback { _ ->
                ComboBoxTableCell<EditTransfer, DelayedCategory>(converter, viewModel.categoriesProperty).apply {
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

        categoryComboBox.apply {

            val categoryConverter = BlockStringConverter<DelayedCategory>(DelayedCategory::name) { PendingCategory(it) }

            cellFactory = TextListCellFactory(categoryConverter::toString)
            buttonCell = cellFactory.call(null)

            items = viewModel.categoriesProperty

            editor.textFormatter = autoCompleteTextFormatter(items, categoryConverter)

            converter = ListLookupStringConverter(items, categoryConverter)

            valueProperty().bindBidirectional(viewModel.selectedCategoryProperty)
        }

        amountTextField.apply {

            val moneyConverter = MoneyStringConverter()

            textFormatter = TextFormatter(moneyConverter).apply {
                valueProperty().bindBidirectional(viewModel.amountProperty)
            }
        }

        memoTextField.apply {
            textProperty().bindBidirectional(viewModel.memoProperty)
        }

        addButton.apply {
            disableProperty().bind(viewModel.addDisabledProperty)
        }

        doneButton.disableProperty().bind(viewModel.doneDisabledProperty)
    }

    fun start(stage: Stage, transfers: ObservableList<EditTransfer>, categories: List<DelayedCategory>) {

        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height

        viewModel.start(transfers, categories)
    }

    @FXML fun onAddButton() {
        viewModel.add()
    }

    @FXML fun onDoneButton() {
        viewModel.done()
        stage.close()
    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
