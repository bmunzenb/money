package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.Statement
import com.munzenberger.money.core.TransactionStatus
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import java.util.function.Consumer

class BalanceAccountViewModel(
        private val statement: Statement,
        entriesViewModel: AccountEntriesViewModel
) : AccountEntriesViewModel by entriesViewModel {

    private val difference = SimpleObjectProperty(Money.ZERO)

    val differenceProperty: ReadOnlyObjectProperty<Money> = difference

    private val transactionsConsumer = Consumer<AsyncObject<List<FXAccountEntry>>> { async ->
        if (async is AsyncObject.Complete) {
            // calculate the difference between the sum of cleared
            // transactions and the expected statement ending balance
            val cleared = async.value
                    .filter { it.statusProperty.value == TransactionStatus.CLEARED }
                    .fold(Money.ZERO) { acc, t -> acc + t.amountProperty.value }

            val total = statement.startingBalance!! + cleared

            difference.value = statement.endingBalance!! - total
        }
    }

    private val transactionsListener = ChangeListener<AsyncObject<List<FXAccountEntry>>> { _, _, newValue ->
        transactionsConsumer.accept(newValue)
    }

    private val weakTransactionsListener = WeakChangeListener(transactionsListener)

    init {
        transactionsConsumer.accept(transactionsProperty.value)
        transactionsProperty.addListener(weakTransactionsListener)
    }
}