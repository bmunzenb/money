package com.munzenberger.money.app

import com.munzenberger.money.app.control.AccountTransactionTableRow
import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCellFactory
import com.munzenberger.money.app.control.TableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.control.bindWaiting
import com.munzenberger.money.app.model.FXRegisterEntry
import com.munzenberger.money.app.model.FXAccountTransactionFilter
import com.munzenberger.money.app.model.moneyNegativePseudoClass
import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.property.NumberStringComparator
import com.munzenberger.money.core.TransactionStatus
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL
import java.time.LocalDate

class AccountRegisterController : AutoCloseable {

    companion object {
        private val LAYOUT: URL = AccountListController::class.java.getResource("AccountRegisterLayout.fxml")

        fun navigation(stage: Stage, database: ObservableMoneyDatabase, accountIdentity: Long) = LayoutControllerNavigation(LAYOUT) {
            controller: AccountRegisterController -> controller.start(stage, database, accountIdentity)
        }
    }

    @FXML lateinit var accountNameProgress: ProgressIndicator
    @FXML lateinit var accountNameLabel: Label
    @FXML lateinit var editAccountButton: Button
    @FXML lateinit var addTransactionButton: Button
    @FXML lateinit var dateFilterChoiceBox: ChoiceBox<FXAccountTransactionFilter>
    @FXML lateinit var tableView: TableView<FXRegisterEntry>
    @FXML lateinit var numberColumn: TableColumn<FXRegisterEntry, String>
    @FXML lateinit var dateColumn: TableColumn<FXRegisterEntry, LocalDate>
    @FXML lateinit var payeeColumn: TableColumn<FXRegisterEntry, String>
    @FXML lateinit var categoryColumn: TableColumn<FXRegisterEntry, String>
    @FXML lateinit var statusColumn: TableColumn<FXRegisterEntry, TransactionStatus>
    @FXML lateinit var debitColumn: TableColumn<FXRegisterEntry, Money>
    @FXML lateinit var creditColumn: TableColumn<FXRegisterEntry, Money>
    @FXML lateinit var balanceColumn: TableColumn<FXRegisterEntry, Money>
    @FXML lateinit var endingBalanceLabel: Label
    @FXML lateinit var endingBalanceProgressIndicator: ProgressIndicator

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase
    private var accountIdentity: Long = -1

    private val viewModel = AccountRegisterViewModel()

    fun initialize() {

        accountNameProgress.visibleProperty().bindAsyncStatus(viewModel.accountProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        accountNameLabel.apply {
            visibleProperty().bindAsyncStatus(viewModel.accountProperty, AsyncObject.Status.COMPLETE)
            textProperty().bindAsync(viewModel.accountProperty) { "Account Register : ${it.name}" }
        }

        editAccountButton.disableProperty().bindAsyncStatus(viewModel.accountProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING,
                AsyncObject.Status.ERROR)

        addTransactionButton.disableProperty().bindAsyncStatus(viewModel.accountProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING,
                AsyncObject.Status.ERROR)

        dateFilterChoiceBox.apply {
            items = viewModel.dateFiltersProperty
            valueProperty().bindBidirectional(viewModel.selectedDateFilterProperty)
        }

        tableView.apply {

            rowFactory = Callback {
                AccountTransactionTableRow(
                        add = this@AccountRegisterController::onAddTransaction,
                        edit = this@AccountRegisterController::onEditTransaction,
                        delete = { onDeleteTransactions(listOf(it)) },
                        markAs = this@AccountRegisterController::updateTransactionStatus
                )
            }

            selectionModel.selectionMode = SelectionMode.SINGLE

            setOnKeyReleased {
                when (it.code) {
                    KeyCode.DELETE -> onDeleteTransactions(selectionModel.selectedItems)
                    else -> Unit
                }
            }

            bindAsync(
                    listProperty = viewModel.transactionsProperty,
                    filterProperty = viewModel.activeFiltersProperty,
                    placeholder = Hyperlink("Add a transaction to get started.").apply {
                        setOnAction { onAddTransaction() }
                    }
            )
        }

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

        categoryColumn.apply {
            cellValueFactory = Callback { it.value.categoryProperty }
        }

        statusColumn.apply {
            cellFactory = TableCellFactory {
                when (it) {
                    TransactionStatus.RECONCILED -> "R"
                    TransactionStatus.CLEARED -> "C"
                    else -> ""
                }
            }
            cellValueFactory = Callback { it.value.statusProperty }
        }

        debitColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false, negativeStyle = false)
            cellValueFactory = Callback { it.value.debitProperty }
            textProperty().bind(viewModel.debitTextProperty)
        }

        creditColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false, negativeStyle = false)
            cellValueFactory = Callback { it.value.creditProperty }
            textProperty().bind(viewModel.creditTextProperty)
        }

        balanceColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false)
            cellValueFactory = Callback { it.value.balanceProperty }
        }

        endingBalanceProgressIndicator.visibleProperty().bindAsyncStatus(viewModel.endingBalanceProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        endingBalanceLabel.apply {
            visibleProperty().bindAsyncStatus(viewModel.endingBalanceProperty, AsyncObject.Status.COMPLETE)
            textProperty().bindAsync(viewModel.endingBalanceProperty) { "Ending Balance: $it" }
        }

        viewModel.endingBalanceProperty.addListener { _, _, newValue ->
            val isMoneyNegative = newValue is AsyncObject.Complete && newValue.value.isNegative
            endingBalanceLabel.pseudoClassStateChanged(moneyNegativePseudoClass, isMoneyNegative)
        }
    }

    fun start(stage: Stage, database: ObservableMoneyDatabase, accountIdentity: Long) {
        this.stage = stage
        this.database = database
        this.accountIdentity = accountIdentity

        // TODO: eventually save as a preference by account
        dateFilterChoiceBox.selectionModel.select(0)

        stage.bindWaiting(viewModel.operationInProgressProperty)

        viewModel.start(database, accountIdentity)
    }

    @FXML fun onEditAccount() {
        viewModel.getAccount {
            DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
                stage.title = editAccountButton.text
                stage.show()
                controller.start(stage, database, it)
            }
        }
    }

    @FXML fun onAddTransaction() {
        viewModel.getAccount {
            startEditTransaction(addTransactionButton.text, Transaction().apply { account = it })
        }
    }

    private fun onEditTransaction(transaction: FXRegisterEntry) {
        viewModel.getTransaction(transaction) { t, e ->
            when {
                t != null -> startEditTransaction("Edit Transaction", t)
                e != null -> ErrorAlert.showAndWait(e)
            }
        }
    }

    private fun onDeleteTransactions(transactions: List<FXRegisterEntry>) {

        Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Confirm Delete"
            contentText = "Are you sure you want to delete this transaction?"
        }.showAndDoWhen(ButtonType.OK) {
            viewModel.deleteTransactions(transactions) { error ->
                if (error != null) {
                    ErrorAlert.showAndWait(error)
                }
            }
        }
    }

    private fun updateTransactionStatus(transaction: FXRegisterEntry, status: TransactionStatus) {
        viewModel.updateTransactionStatus(transaction, status) { error ->
            if (error != null) {
                ErrorAlert.showAndWait(error)
            }
        }
    }

    private fun startEditTransaction(title: String, transaction: Transaction) {
        DialogBuilder.build(EditTransactionController.LAYOUT) { stage, controller: EditTransactionController ->
            stage.title = title
            stage.show()
            controller.start(stage, database, transaction)
        }
    }

    override fun close() {
        viewModel.close()
    }
}

private fun AccountRegisterViewModel.getAccount(block: (Account) -> Unit) = accountProperty.value.let {
    when (it) {
        is AsyncObject.Complete -> block.invoke(it.value)
        else -> Unit
    }
}
