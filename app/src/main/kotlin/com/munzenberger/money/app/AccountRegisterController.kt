package com.munzenberger.money.app

import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCell
import com.munzenberger.money.app.control.MoneyTableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.model.FXTransactionDetail
import com.munzenberger.money.app.navigation.LayoutControllerNavigation
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.isNegative
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL
import java.text.DateFormat
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
    @FXML lateinit var tableView: TableView<FXTransactionDetail>
    @FXML lateinit var dateColumn: TableColumn<FXTransactionDetail, Date>
    @FXML lateinit var payeeColumn: TableColumn<FXTransactionDetail, String>
    @FXML lateinit var categoryColumn: TableColumn<FXTransactionDetail, String>
    @FXML lateinit var paymentColumn: TableColumn<FXTransactionDetail, Money>
    @FXML lateinit var depositColumn: TableColumn<FXTransactionDetail, Money>
    @FXML lateinit var balanceColumn: TableColumn<FXTransactionDetail, Money>
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

        tableView.bindAsync(viewModel.transactionsProperty) {
            Hyperlink("Add a transaction to get started.").apply {
                setOnAction { addTransaction() }
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

        paymentColumn.apply {
            cellFactory = MoneyTableCellFactory()
            cellValueFactory = Callback { it.value.paymentProperty }
            // TODO: set title of column based on account type
        }

        depositColumn.apply {
            cellFactory = MoneyTableCellFactory()
            cellValueFactory = Callback { it.value.depositProperty }
            // TODO: set title of column based on account type
        }

        balanceColumn.apply {
            cellFactory = MoneyTableCellFactory()
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
            endingBalanceLabel.styleClass.remove(MoneyTableCell.NEGATIVE_STYLE_CLASS)
            when (newValue) {
                is AsyncObject.Complete<Money> -> {
                    if (newValue.value.isNegative) {
                        endingBalanceLabel.styleClass.add(MoneyTableCell.NEGATIVE_STYLE_CLASS)
                    }
                }
            }
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

    @FXML fun addTransaction() {
        viewModel.getAccount {
            DialogBuilder.build(EditTransactionController.LAYOUT) { stage, controller: EditTransactionController ->
                stage.title = addTransactionButton.text
                stage.show()
                controller.start(stage, database, Transaction().apply { account = it })
            }
        }
    }

    override fun close() {
        viewModel.close()
    }
}
