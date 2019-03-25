package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.model.FXAccountType
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL

class AccountListController {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("AccountListLayout.fxml")
    }

    @FXML lateinit var createAccountButton: Button
    @FXML lateinit var tableView: TableView<FXAccount>
    @FXML lateinit var nameColumn: TableColumn<FXAccount, String>
    @FXML lateinit var typeColumn: TableColumn<FXAccount, FXAccountType>
    @FXML lateinit var numberColumn: TableColumn<FXAccount, String?>
    @FXML lateinit var balanceColumn: TableColumn<FXAccount, AsyncObject<Money>>
    @FXML lateinit var totalBalanceProgress: ProgressIndicator
    @FXML lateinit var totalBalanceLabel: Label

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase

    private val viewModel = AccountListViewModel()

    fun initialize() {

        tableView.items = FXCollections.observableArrayList<FXAccount>().apply {
            bindAsync(viewModel.accountsProperty)
        }

        nameColumn.apply {
            cellFactory = TableCellFactory.hyperlink(action = {})
            cellValueFactory = Callback { a -> a.value.nameProperty }
        }

        typeColumn.apply {
            cellFactory = TableCellFactory.text { it?.nameProperty?.get() }
            cellValueFactory = Callback { a -> a.value.typeProperty }
        }

        numberColumn.apply {
            cellFactory = TableCellFactory.text { it?.sanitize() }
            cellValueFactory = Callback { a -> a.value.numberProperty }
        }

        balanceColumn.apply {
            cellFactory = AsyncTableViewCellFactory.text()
            cellValueFactory = Callback { a -> a.value.balanceProperty }
        }

        totalBalanceProgress.visibleProperty().bindAsyncStatus(viewModel.totalBalanceProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        totalBalanceLabel.visibleProperty().bindAsyncStatus(viewModel.totalBalanceProperty,
                AsyncObject.Status.COMPLETE,
                AsyncObject.Status.ERROR)

        totalBalanceLabel.textProperty().bindAsync(viewModel.totalBalanceProperty) { "Total Account Balance: $it" }
    }

    fun start(stage: Stage, database: MoneyDatabase) {
        this.stage = stage
        this.database = database

        viewModel.start(database)
    }

    // TODO: find a nice way to call this when this controller is no longer loaded
    fun clear() {
        viewModel.clear()
    }

    @FXML fun onCreateAccount() {

        DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
            stage.title = createAccountButton.text
            stage.show()
            controller.start(stage, database, Account())
        }
    }
}
