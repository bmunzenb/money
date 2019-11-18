package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.SplitCategory
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.Transaction
import com.munzenberger.money.core.Transfer
import com.munzenberger.money.sql.TransactionQueryExecutor
import io.reactivex.Completable
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener

class EditTransfersViewModel(
        private val typeDisabled: BooleanProperty,
        private val selectedType: ObjectProperty<TransactionType?>,
        private val categoryDisabled: BooleanProperty,
        private val selectedCategory: ObjectProperty<DelayedCategory?>,
        private val amountDisabled: BooleanProperty,
        private val amount: ObjectProperty<Money>
) {

    private lateinit var transfers: List<Transfer>

    private val transferViewModels = FXCollections.observableArrayList<TransferViewModel>().apply {
        addListener(ListChangeListener {
            when {
                it.list.size == 1 -> {
                    // single transfer transaction
                    typeDisabled.value = false
                    categoryDisabled.value = false
                    amountDisabled.value = false
                }
                it.list.size > 1 -> {
                    // split transaction
                    typeDisabled.value = true
                    selectedType.value = TransactionType.Split
                    categoryDisabled.value = true
                    selectedCategory.value = SplitCategory
                    amountDisabled.value = true
                    // TODO: set amount equal to the sum of the transfers
                }
            }
        })
    }

    fun setTransfers(transfers: List<Transfer>) {

        this.transfers = when {
            // make sure there's at least one transfer
            transfers.isEmpty() -> listOf(Transfer())
            else -> transfers
        }

        transferViewModels.setAll(this.transfers.map { TransferViewModel(it) })
    }

    fun save(transaction: Transaction, tx: TransactionQueryExecutor) = Completable.fromAction {

    }
}

class TransferViewModel(transfer: Transfer) {

}
