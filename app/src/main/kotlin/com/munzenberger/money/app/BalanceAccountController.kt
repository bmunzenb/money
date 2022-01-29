package com.munzenberger.money.app

import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.property.NumberStringComparator
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.TransactionStatus
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL
import java.time.LocalDate
import java.util.function.Predicate

class BalanceAccountController  {

    companion object {
        val LAYOUT: URL = BalanceAccountController::class.java.getResource("BalanceAccountLayout.fxml")!!
    }

    @FXML lateinit var transactionsTable: TableView<FXAccountEntry>
    @FXML lateinit var numberColumn: TableColumn<FXAccountEntry, String>
    @FXML lateinit var dateColumn: TableColumn<FXAccountEntry, LocalDate>
    @FXML lateinit var payeeColumn: TableColumn<FXAccountEntry, String>
    @FXML lateinit var statusColumn: TableColumn<FXAccountEntry, TransactionStatus>
    @FXML lateinit var debitColumn: TableColumn<FXAccountEntry, Money>
    @FXML lateinit var creditColumn: TableColumn<FXAccountEntry, Money>
    @FXML lateinit var continueButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var viewModel: BalanceAccountViewModel
    private lateinit var stage: Stage

    fun initialize() {

        numberColumn.apply {
            cellValueFactory = Callback { it.value.numberProperty }
            comparator = NumberStringComparator
        }

        dateColumn.apply {
            cellFactory = DateTableCellFactory()
            cellValueFactory = Callback { it.value.dateProperty }
        }

        payeeColumn.apply {
            cellValueFactory = Callback { it.value.payeeProperty }
        }

        statusColumn.apply {
            cellValueFactory = Callback { it.value.statusProperty }
        }

        debitColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false, negativeStyle = false)
            cellValueFactory = Callback { it.value.debitProperty }
        }

        creditColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false, negativeStyle = false)
            cellValueFactory = Callback { it.value.creditProperty }
        }
    }

    fun start(stage: Stage, entriesViewModel: AccountEntriesViewModel) {

        this.viewModel = BalanceAccountViewModel(entriesViewModel)
        this.stage = stage

        stage.minWidth = stage.width
        stage.minHeight = stage.height

        transactionsTable.apply {

            selectionModel.selectionMode = SelectionMode.SINGLE

            // only show unreconciled transactions
            val filter = Predicate<FXAccountEntry> { it.statusProperty.value != TransactionStatus.RECONCILED }

            bindAsync(
                    listProperty = viewModel.transactionsProperty,
                    filterProperty = SimpleObjectProperty(filter),
                    placeholder = Text("No transactions.")
            )
        }

        debitColumn.textProperty().bind(viewModel.debitTextProperty)

        creditColumn.textProperty().bind(viewModel.creditTextProperty)

        viewModel.start()
    }

    @FXML fun onContinueButton() {

    }

    @FXML fun onCancelButton() {
        stage.close()
    }
}
