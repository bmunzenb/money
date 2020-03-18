package com.munzenberger.money.app

import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.HyperlinkTableCellFactory
import com.munzenberger.money.app.control.bindAsyncProperty
import com.munzenberger.money.app.model.FXPayee
import com.munzenberger.money.app.property.AsyncObjectMapper
import com.munzenberger.money.app.property.bindAsync
import com.munzenberger.money.core.rx.ObservableMoneyDatabase
import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.Callback
import java.net.URL
import java.util.Date

class PayeeListController : AutoCloseable {

    companion object {
        val LAYOUT: URL = PayeeListController::class.java.getResource("PayeeListLayout.fxml")
    }

    @FXML lateinit var tableView: TableView<FXPayee>
    @FXML lateinit var nameColumn: TableColumn<FXPayee, String>
    @FXML lateinit var lastPaidColumn: TableColumn<FXPayee, Date>

    private val viewModel = PayeeListViewModel()

    fun initialize() {

        tableView.bindAsyncProperty(viewModel.payeesProperty) {
            Text("No payees.")
        }

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
