package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.MoneyDatabase
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
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
    @FXML lateinit var typeColumn: TableColumn<FXAccount, String>
    @FXML lateinit var numberColumn: TableColumn<FXAccount, String?>
    @FXML lateinit var balanceColumn: TableColumn<FXAccount, Long>

    private lateinit var stage: Stage
    private lateinit var database: MoneyDatabase

    private val viewModel = AccountListViewModel()

    fun initialize() {

        tableView.items = FXCollections.observableArrayList<FXAccount>().apply {
            bindAsync(viewModel.accountsProperty)
        }

        nameColumn.apply {
            cellFactory = TableCellFactory.string { it }
            cellValueFactory = Callback { a -> a.value.nameProperty }
        }
    }

    fun start(stage: Stage, database: MoneyDatabase) {
        this.stage = stage
        this.database = database

        viewModel.start(database)
    }

    @FXML fun onCreateAccount() {

        DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
            stage.title = createAccountButton.text
            stage.show()
            controller.start(stage, database, Account())
        }
    }
}
