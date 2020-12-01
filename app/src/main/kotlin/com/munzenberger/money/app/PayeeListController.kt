package com.munzenberger.money.app

import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.HyperlinkTableCellFactory
import com.munzenberger.money.app.control.bindAsync
import com.munzenberger.money.app.model.FXPayee
import com.munzenberger.money.app.database.ObservableMoneyDatabase
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.text.Text
import javafx.util.Callback
import java.net.URL
import java.time.LocalDate

class PayeeListController : AutoCloseable {

    companion object {
        val LAYOUT: URL = PayeeListController::class.java.getResource("PayeeListLayout.fxml")
    }

    @FXML lateinit var tableView: TableView<FXPayee>
    @FXML lateinit var nameColumn: TableColumn<FXPayee, String>
    @FXML lateinit var lastPaidColumn: TableColumn<FXPayee, LocalDate>

    private val viewModel = PayeeListViewModel()

    fun initialize() {

        tableView.bindAsync(
                listProperty = viewModel.payeesProperty,
                placeholder = Text("No payees.")
        )

        nameColumn.apply {
            cellFactory = HyperlinkTableCellFactory { /* TODO: open the payee details screen */ }
            cellValueFactory = Callback { p -> p.value.nameProperty }
        }

        lastPaidColumn.apply {
            cellFactory = DateTableCellFactory()
            cellValueFactory = Callback { p -> p.value.lastPaidProperty }
        }
    }

    fun start(database: ObservableMoneyDatabase) {
        viewModel.start(database)
    }

    @FXML fun onAddPayee() {

    }

    override fun close() {
        viewModel.close()
    }
}
