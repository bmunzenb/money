package com.munzenberger.money.app

import com.munzenberger.money.app.control.AccountTransactionTableRow
import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.model.FXAccountTransaction
import com.munzenberger.money.app.model.delete
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
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
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
import java.util.Date

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
    @FXML lateinit var tableView: TableView<FXAccountTransaction>
    @FXML lateinit var dateColumn: TableColumn<FXAccountTransaction, Date>
    @FXML lateinit var payeeColumn: TableColumn<FXAccountTransaction, String>
    @FXML lateinit var categoryColumn: TableColumn<FXAccountTransaction, String>
    @FXML lateinit var debitColumn: TableColumn<FXAccountTransaction, Money>
    @FXML lateinit var creditColumn: TableColumn<FXAccountTransaction, Money>
    @FXML lateinit var balanceColumn: TableColumn<FXAccountTransaction, Money>
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

        tableView.apply {

            rowFactory = Callback {
                AccountTransactionTableRow(
                        addTransaction = this@AccountRegisterController::onAddTransaction,
                        editTransaction = this@AccountRegisterController::onEditTransaction,
                        deleteTransactions = this@AccountRegisterController::onDeleteTransactions
                )
            }

            selectionModel.selectionMode = SelectionMode.SINGLE

            setOnKeyReleased {
                when (it.code) {
                    KeyCode.DELETE -> onDeleteTransactions(selectionModel.selectedItems)
                    else -> Unit
                }
            }

            dateColumn.prefWidthProperty().bind(widthProperty().multiply(0.09))
            payeeColumn.prefWidthProperty().bind(widthProperty().multiply(0.27))
            categoryColumn.prefWidthProperty().bind(widthProperty().multiply(0.27))
            debitColumn.prefWidthProperty().bind(widthProperty().multiply(0.12))
            creditColumn.prefWidthProperty().bind(widthProperty().multiply(0.12))
            balanceColumn.prefWidthProperty().bind(widthProperty().multiply(0.12))

            dateColumn.isResizable = false
            payeeColumn.isResizable = false
            categoryColumn.isResizable = false
            debitColumn.isResizable = false
            creditColumn.isResizable = false
            balanceColumn.isResizable = false

            bindAsync(viewModel.transactionsProperty) {
                Hyperlink("Add a transaction to get started.").apply {
                    setOnAction { onAddTransaction() }
                }
            }
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

    private fun onEditTransaction(transaction: FXAccountTransaction) {
        stage.scene.cursor = Cursor.WAIT
        transaction.getTransaction(database) { t, e ->
            stage.scene.cursor = Cursor.DEFAULT
            when {
                t != null -> startEditTransaction("Edit Transaction", t)
                e != null -> ErrorAlert.showAndWait(e)
            }
        }
    }

    private fun onDeleteTransactions(transactions: List<FXAccountTransaction>) {

        val result = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Confirm Delete"
            contentText = "Are you sure you want to delete this transaction?"
        }.showAndWait()

        when {
            result.isPresent && result.get() == ButtonType.OK -> {
                stage.scene.cursor = Cursor.WAIT
                transactions.delete(database) { error ->
                    stage.scene.cursor = Cursor.DEFAULT
                    if (error != null) {
                        ErrorAlert.showAndWait(error)
                    }
                }
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
