package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.model.FXAccountType
import com.munzenberger.money.app.navigation.Navigator
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.AsyncObjectMapper
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Callback
import java.net.URL

class AccountListController : AutoCloseable {

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
    private lateinit var navigator: Navigator

    private val viewModel = AccountListViewModel()
    private val retainListeners = mutableListOf<ChangeListener<*>>()

    fun initialize() {

        tableView.apply {

            items = FXCollections.observableArrayList<FXAccount>().apply {
                retainListeners += bindAsync(viewModel.accountsProperty)
            }

            retainListeners += placeholderProperty().bindAsync(viewModel.accountsProperty, object : AsyncObjectMapper<List<FXAccount>, Node> {

                override fun pending() = executing()

                override fun executing() = ProgressIndicator().apply {
                    setPrefSize(60.0, 60.0)
                    setMaxSize(60.0, 60.0)
                }

                override fun complete(obj: List<FXAccount>) = Hyperlink("Create an account to get started.").apply {
                    setOnAction { onCreateAccount() }
                }

                override fun error(error: Throwable) = Label(error.message).apply {
                    textFill = Color.RED
                }
            })
        }

        nameColumn.apply {
            cellFactory = TableCellFactory.hyperlink(action = {
                navigator.goTo(AccountRegisterController.navigation(stage, database, it.identity))
            })
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

        retainListeners += totalBalanceProgress.visibleProperty().bindAsyncStatus(viewModel.totalBalanceProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        totalBalanceLabel.apply {
            retainListeners += visibleProperty().bindAsyncStatus(viewModel.totalBalanceProperty, AsyncObject.Status.COMPLETE)
            retainListeners += textProperty().bindAsync(viewModel.totalBalanceProperty) { "Total Account Balance: $it" }
            // TODO: what to display if there's an error?
        }
    }

    fun start(stage: Stage, database: MoneyDatabase, navigator: Navigator) {
        this.stage = stage
        this.database = database
        this.navigator = navigator

        viewModel.start(database)
    }

    override fun close() {
        viewModel.close()
    }

    @FXML fun onCreateAccount() {

        DialogBuilder.build(EditAccountController.LAYOUT) { stage, controller: EditAccountController ->
            stage.title = createAccountButton.text
            stage.show()
            controller.start(stage, database, Account())
        }
    }
}
