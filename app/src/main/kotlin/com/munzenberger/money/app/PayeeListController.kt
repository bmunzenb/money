package com.munzenberger.money.app

import com.munzenberger.money.app.control.DateTableCellFactory
import com.munzenberger.money.app.control.HyperlinkTableCellFactory
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

        tableView.apply {

            val payeeList = FXCollections.observableArrayList<FXPayee>().apply {
                bindAsync(viewModel.payeesProperty)
            }

            val sortedList = SortedList(payeeList)

            // keep the table sorted when the contents change
            sortedList.comparatorProperty().bind(comparatorProperty())

            items = sortedList

            placeholderProperty().bindAsync(viewModel.payeesProperty, object : AsyncObjectMapper<List<FXPayee>, Node> {

                override fun pending() = executing()

                override fun executing() = ProgressIndicator().apply {
                    setPrefSize(60.0, 60.0)
                    setMaxSize(60.0, 60.0)
                }

                override fun complete(obj: List<FXPayee>) = Text("No payees.")

                override fun error(error: Throwable) = Label(error.message).apply {
                    textFill = Color.RED
                }
            })
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
