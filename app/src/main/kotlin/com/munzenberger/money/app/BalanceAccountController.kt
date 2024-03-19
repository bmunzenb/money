package com.munzenberger.money.app

import com.munzenberger.money.app.control.CheckBoxTableViewCellFactory
import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.model.moneyNegativePseudoClass
import com.munzenberger.money.app.property.NumberStringComparator
import com.munzenberger.money.app.property.toBinding
import com.munzenberger.money.core.*
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.GridPane
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

    @FXML lateinit var container: GridPane
    @FXML lateinit var transactionsTable: TableView<FXAccountEntry>
    @FXML lateinit var numberColumn: TableColumn<FXAccountEntry, String>
    @FXML lateinit var dateColumn: TableColumn<FXAccountEntry, LocalDate>
    @FXML lateinit var payeeColumn: TableColumn<FXAccountEntry, String>
    @FXML lateinit var statusColumn: TableColumn<FXAccountEntry, TransactionStatus>
    @FXML lateinit var amountColumn: TableColumn<FXAccountEntry, Money>
    @FXML lateinit var balanceColumn: TableColumn<FXAccountEntry, Money>
    @FXML lateinit var statementBalanceLabel: Label
    @FXML lateinit var clearedBalanceLabel: Label
    @FXML lateinit var differenceLabel: Label
    @FXML lateinit var continueButton: Button
    @FXML lateinit var cancelButton: Button

    private lateinit var viewModel: BalanceAccountViewModel
    // TODO remove reference to stage in controller
    private lateinit var stage: Stage

    private lateinit var onReconciled: Runnable

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
            cellValueFactory = Callback {
                Bindings.`when`(it.value.payeeProperty.isEmpty)
                        .then(it.value.categoryProperty.value)
                        .otherwise(it.value.payeeProperty.value)
            }
        }

        statusColumn.apply {
            cellFactory = CheckBoxTableViewCellFactory(
                    isChecked = { it == TransactionStatus.CLEARED },
                    onChanged = ::changeStatus
            )
            cellValueFactory = Callback { it.value.statusProperty }
        }

        amountColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false)
            cellValueFactory = Callback { it.value.amountProperty }
        }

        balanceColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false)
            cellValueFactory = Callback { it.value.balanceProperty }
        }
    }

    fun start(
        stage: Stage,
        database: MoneyDatabase,
        statement: Statement,
        entriesViewModel: AccountEntriesViewModel,
        onReconciled: Runnable
    ) {

        this.viewModel = BalanceAccountViewModel(database, statement, entriesViewModel)
        this.stage = stage
        this.onReconciled = onReconciled

        stage.apply {
            scene.stylesheets.add(MoneyApplication.CSS)
            minWidth = width
            minHeight = height
        }

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

        amountColumn.textProperty().bind(viewModel.amountTextProperty)

        statementBalanceLabel.textProperty().bind(viewModel.statementBalanceProperty.toBinding {
            statementBalanceLabel.pseudoClassStateChanged(moneyNegativePseudoClass, it.isNegative)
            it.toStringWithoutCurrency()
        })

        clearedBalanceLabel.textProperty().bind(viewModel.clearedBalanceProperty.toBinding {
            clearedBalanceLabel.pseudoClassStateChanged(moneyNegativePseudoClass, it.isNegative)
            it.toStringWithoutCurrency()
        })

        differenceLabel.textProperty().bind(viewModel.differenceProperty.toBinding {
            differenceLabel.pseudoClassStateChanged(moneyNegativePseudoClass, it.isNegative)
            it.toStringWithoutCurrency()
        })

        continueButton.disableProperty().bind(viewModel.continueDisabledBinding)

        container.disableProperty().bind(viewModel.isOperationInProgressProperty)
    }

    @FXML fun onContinueButton() {
        viewModel.reconcile {
            when (it) {
                null -> {
                    stage.close()
                    onReconciled.run()
                }
                else -> ErrorAlert.showAndWait(it)
            }
        }
    }

    @FXML fun onCancelButton() {
        stage.close()
    }

    private fun changeStatus(entry: FXAccountEntry, isCleared: Boolean) {

        val status = if (isCleared) TransactionStatus.CLEARED else TransactionStatus.UNRECONCILED

        viewModel.updateEntryStatus(entry, status) { error ->
            if (error != null) { ErrorAlert.showAndWait(error) }
        }
    }
}
