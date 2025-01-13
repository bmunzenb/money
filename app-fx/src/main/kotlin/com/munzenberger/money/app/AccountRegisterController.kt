package com.munzenberger.money.app

import com.munzenberger.money.app.control.AccountEntryTableRow
import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCellFactory
import com.munzenberger.money.app.control.TableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.control.booleanToWaitCursor
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.model.FXAccountEntryFilter
import com.munzenberger.money.app.model.FXTransactionAccountEntry
import com.munzenberger.money.app.model.FXTransferAccountEntry
import com.munzenberger.money.app.model.moneyNegativePseudoClass
import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.NumberStringComparator
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.app.property.bindAsyncValue
import com.munzenberger.money.app.property.withValue
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountIdentity
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.Statement
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.TransactionStatus
import com.munzenberger.money.core.TransferEntryIdentity
import com.munzenberger.money.core.isNegative
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
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AccountRegisterController : AutoCloseable {
    companion object {
        private val LAYOUT: URL = AccountRegisterController::class.java.getResource("AccountRegisterLayout.fxml")!!

        fun navigation(
            stage: Stage,
            database: ObservableMoneyDatabase,
            accountIdentity: AccountIdentity,
        ) = LayoutControllerNavigation(LAYOUT) { controller: AccountRegisterController ->
            controller.start(stage, database, accountIdentity)
        }
    }

    @FXML lateinit var accountNameProgress: ProgressIndicator

    @FXML lateinit var accountNameLabel: Label

    @FXML lateinit var editAccountButton: Button

    @FXML lateinit var addTransactionButton: Button

    @FXML lateinit var balanceAccountButton: Button

    @FXML lateinit var dateFilterChoiceBox: ChoiceBox<FXAccountEntryFilter>

    @FXML lateinit var statusFilterChoiceBox: ChoiceBox<FXAccountEntryFilter>

    @FXML lateinit var tableView: TableView<FXAccountEntry>

    @FXML lateinit var numberColumn: TableColumn<FXAccountEntry, String>

    @FXML lateinit var dateColumn: TableColumn<FXAccountEntry, LocalDate>

    @FXML lateinit var payeeColumn: TableColumn<FXAccountEntry, String>

    @FXML lateinit var categoryColumn: TableColumn<FXAccountEntry, String>

    @FXML lateinit var statusColumn: TableColumn<FXAccountEntry, TransactionStatus>

    @FXML lateinit var amountColumn: TableColumn<FXAccountEntry, Money>

    @FXML lateinit var balanceColumn: TableColumn<FXAccountEntry, Money>

    @FXML lateinit var endingBalanceLabel: Label

    @FXML lateinit var endingBalanceProgressIndicator: ProgressIndicator

    // TODO remove reference to stage in controller
    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase

    private val viewModel = AccountRegisterViewModel()

    fun initialize() {
        accountNameProgress.visibleProperty().bindAsyncStatus(
            viewModel.accountProperty,
            AsyncObject.Status.PENDING,
            AsyncObject.Status.EXECUTING,
        )

        accountNameLabel.apply {
            visibleProperty().bindAsyncStatus(viewModel.accountProperty, AsyncObject.Status.COMPLETE)
            textProperty().bindAsyncValue(viewModel.accountProperty) { it.name }
        }

        editAccountButton.disableProperty().bindAsyncStatus(
            viewModel.accountProperty,
            AsyncObject.Status.PENDING,
            AsyncObject.Status.EXECUTING,
            AsyncObject.Status.ERROR,
        )

        addTransactionButton.disableProperty().bindAsyncStatus(
            viewModel.accountProperty,
            AsyncObject.Status.PENDING,
            AsyncObject.Status.EXECUTING,
            AsyncObject.Status.ERROR,
        )

        balanceAccountButton.disableProperty().bindAsyncStatus(
            viewModel.accountProperty,
            AsyncObject.Status.PENDING,
            AsyncObject.Status.EXECUTING,
            AsyncObject.Status.ERROR,
        )

        dateFilterChoiceBox.apply {
            items = viewModel.dateFiltersProperty
            valueProperty().bindBidirectional(viewModel.selectedDateFilterProperty)
        }

        statusFilterChoiceBox.apply {
            items = viewModel.statusFiltersProperty
            valueProperty().bindBidirectional(viewModel.selectedStatusFilterProperty)
        }

        tableView.apply {
            rowFactory =
                Callback {
                    AccountEntryTableRow { action ->
                        when (action) {
                            is AccountEntryTableRow.Action.Add -> onAddTransaction()
                            is AccountEntryTableRow.Action.Edit -> editEntry(action.entry)
                            is AccountEntryTableRow.Action.Delete -> deleteEntry(action.entry)
                            is AccountEntryTableRow.Action.UpdateStatus -> updateEntryStatus(action.entry, action.status)
                        }
                    }
                }

            selectionModel.selectionMode = SelectionMode.SINGLE

            bindAsync(
                listProperty = viewModel.transactionsProperty,
                filterProperty = viewModel.activeFiltersProperty,
                placeholder =
                    Hyperlink("Add a transaction to get started.").apply {
                        setOnAction { onAddTransaction() }
                    },
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
            cellFactory =
                TableCellFactory {
                    when (it) {
                        TransactionStatus.RECONCILED -> "R"
                        TransactionStatus.CLEARED -> "C"
                        else -> ""
                    }
                }
            cellValueFactory = Callback { it.value.statusProperty }
        }

        amountColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false)
            cellValueFactory = Callback { it.value.amountProperty }
            textProperty().bind(viewModel.amountTextProperty)
        }

        balanceColumn.apply {
            cellFactory = MoneyTableCellFactory(withCurrency = false)
            cellValueFactory = Callback { it.value.balanceProperty }
        }

        endingBalanceProgressIndicator.visibleProperty().bindAsyncStatus(
            viewModel.endingBalanceProperty,
            AsyncObject.Status.PENDING,
            AsyncObject.Status.EXECUTING,
        )

        endingBalanceLabel.apply {
            visibleProperty().bindAsyncStatus(viewModel.endingBalanceProperty, AsyncObject.Status.COMPLETE)
            // TODO split label and value into separate nodes and dynamically set label based on account type
            textProperty().bindAsyncValue(viewModel.endingBalanceProperty) { "Ending Balance: $it" }
        }

        viewModel.endingBalanceProperty.addListener { _, _, newValue ->
            val isMoneyNegative = newValue is AsyncObject.Complete && newValue.value.isNegative
            endingBalanceLabel.pseudoClassStateChanged(moneyNegativePseudoClass, isMoneyNegative)
        }
    }

    fun start(
        stage: Stage,
        database: ObservableMoneyDatabase,
        accountIdentity: AccountIdentity,
    ) {
        this.stage = stage
        this.database = database

        // TODO: save as preferences by account and restore here
        // dateFilterChoiceBox.selectionModel.select(0)
        // statusFilterChoiceBox.selectionModel.select(0)

        viewModel.isOperationInProgressProperty.addListener { _, _, newValue ->
            stage.scene.cursor = booleanToWaitCursor(newValue)
            stage.scene.root.isDisable = newValue
        }

        viewModel.start(database, accountIdentity)
    }

    @FXML fun onEditAccount() {
        viewModel.accountProperty.value.withValue {
            DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
                stage.title = editAccountButton.text
                stage.show()
                controller.start(stage, database, it)
            }
        }
    }

    @FXML fun onAddTransaction() {
        viewModel.accountProperty.value.withValue {
            startEditTransaction(addTransactionButton.text, Transaction().apply { account = it })
        }
    }

    @FXML fun onBalanceAccount() {
        viewModel.accountProperty.value.withValue {
            startBalanceAccount(it)
        }
    }

    private fun editEntry(entry: FXAccountEntry) {
        viewModel.prepareEditEntry(entry) {
            when (it) {
                is AccountRegisterViewModel.Edit.Transaction ->
                    startEditTransaction("Edit Transaction", it.transaction)

                is AccountRegisterViewModel.Edit.Transfer ->
                    startEditTransfer(it.transferId)

                is AccountRegisterViewModel.Edit.Error ->
                    ErrorAlert.showAndWait(it.error)
            }
        }
    }

    private fun deleteEntry(entry: FXAccountEntry) {
        val contentText =
            when (entry) {
                is FXTransactionAccountEntry -> "Are you sure you want to delete this transaction?"
                is FXTransferAccountEntry -> "Are you sure you want to delete this transfer?"
            }

        val result =
            Alert(Alert.AlertType.CONFIRMATION).let {
                it.title = "Confirm Delete"
                it.headerText = null
                it.contentText = contentText
                it.showAndWait()
            }

        if (result.isPresent && result.get() == ButtonType.OK) {
            viewModel.deleteEntry(entry) { error ->
                error?.let { ErrorAlert.showAndWait(it) }
            }
        }
    }

    private fun updateEntryStatus(
        entry: FXAccountEntry,
        status: TransactionStatus,
    ) {
        viewModel.updateEntryStatus(entry, status) { error ->
            error?.let { ErrorAlert.showAndWait(it) }
        }
    }

    private fun startEditTransaction(
        title: String,
        transaction: Transaction,
    ) {
        DialogBuilder.build(EditTransactionController.LAYOUT) { stage, controller: EditTransactionController ->
            stage.title = title
            stage.show()
            controller.start(stage, database, transaction)
        }
    }

    private fun startEditTransfer(transferId: TransferEntryIdentity) {
        DialogBuilder.build(EditTransferController.LAYOUT) { stage, controller: EditTransferController ->
            stage.title = "Edit Transfer"
            stage.show()
            controller.start(stage, database, transferId)
        }
    }

    private fun startBalanceAccount(account: Account) {
        DialogBuilder.build(BalanceStatementController.LAYOUT) { stage, controller: BalanceStatementController ->
            stage.title = "Balance ${account.name}"
            stage.show()
            controller.start(stage, database, account) { statement ->
                DialogBuilder.build(BalanceAccountController.LAYOUT) { stage, controller: BalanceAccountController ->
                    stage.title = "Balance ${account.name}"
                    stage.show()
                    controller.start(stage, database, statement, viewModel) {
                        onStatementReconciled(account, statement)
                    }
                }
            }
        }
    }

    private fun onStatementReconciled(
        account: Account,
        statement: Statement,
    ) {
        val formattedDate =
            DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .format(statement.closingDate)

        Alert(Alert.AlertType.INFORMATION).apply {
            title = "Balance ${account.name}"
            headerText = "Balanced!"
            contentText = "You have balanced your '${account.name}' account through $formattedDate."
            showAndWait()
        }
    }

    override fun close() {
        viewModel.close()
    }
}
