package com.munzenberger.money.app

import com.munzenberger.money.app.control.HyperlinkTableCellFactory
import com.munzenberger.money.app.control.MoneyAsyncTableCellFactory
import com.munzenberger.money.app.control.MoneyTableCell
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.model.FXAccount
import com.munzenberger.money.app.navigation.Navigator
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.AsyncObjectComparator
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.app.property.bindAsyncStatus
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
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

class AccountListController : AutoCloseable {

    companion object {
        val LAYOUT: URL = AccountListController::class.java.getResource("AccountListLayout.fxml")
    }

    @FXML lateinit var createAccountButton: Button
    @FXML lateinit var tableView: TableView<FXAccount>
    @FXML lateinit var nameColumn: TableColumn<FXAccount, String>
    @FXML lateinit var typeColumn: TableColumn<FXAccount, String>
    @FXML lateinit var numberColumn: TableColumn<FXAccount, String?>
    @FXML lateinit var balanceColumn: TableColumn<FXAccount, AsyncObject<Money>>
    @FXML lateinit var totalBalanceProgress: ProgressIndicator
    @FXML lateinit var totalBalanceLabel: Label

    private lateinit var stage: Stage
    private lateinit var database: ObservableMoneyDatabase
    private lateinit var navigator: Navigator

    private val viewModel = AccountListViewModel()

    fun initialize() {

        tableView.bindAsync(viewModel.accountsProperty) {
            Hyperlink("Create an account to get started.").apply {
                setOnAction { onCreateAccount() }
            }
        }

        nameColumn.apply {
            cellFactory = HyperlinkTableCellFactory {
                navigator.goTo(AccountRegisterController.navigation(stage, database, it.identity))
            }
            cellValueFactory = Callback { a -> a.value.nameProperty }
        }

        typeColumn.apply {
            cellValueFactory = Callback { a -> a.value.typeProperty.get().nameProperty }
        }

        numberColumn.apply {
            cellValueFactory = Callback { a -> a.value.numberProperty }
        }

        balanceColumn.apply {
            cellFactory = MoneyAsyncTableCellFactory()
            cellValueFactory = Callback { a -> a.value.balanceProperty }
            comparator = AsyncObjectComparator()
        }

        totalBalanceProgress.visibleProperty().bindAsyncStatus(viewModel.totalBalanceProperty,
                AsyncObject.Status.PENDING,
                AsyncObject.Status.EXECUTING)

        totalBalanceLabel.apply {
            visibleProperty().bindAsyncStatus(viewModel.totalBalanceProperty, AsyncObject.Status.COMPLETE)
            textProperty().bindAsync(viewModel.totalBalanceProperty) { "Total Account Balance: $it" }
        }

        viewModel.totalBalanceProperty.addListener { _, _, newValue ->
            totalBalanceLabel.styleClass.remove(MoneyTableCell.NEGATIVE_STYLE_CLASS)
            when (newValue) {
                is AsyncObject.Complete<Money> -> {
                    if (newValue.value.isNegative) {
                        totalBalanceLabel.styleClass.add(MoneyTableCell.NEGATIVE_STYLE_CLASS)
                    }
                    tableView.sort()
                }
            }
        }
    }

    fun start(stage: Stage, database: ObservableMoneyDatabase, navigator: Navigator) {
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
