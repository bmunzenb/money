package com.munzenberger.money.app

import com.munzenberger.money.app.model.FXAccountEntry
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.Statement
import com.munzenberger.money.core.TransactionStatus
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import java.util.concurrent.Callable
import java.util.function.Consumer

class BalanceAccountViewModel(
        private val statement: Statement,
        entriesViewModel: AccountEntriesViewModel
) : AccountEntriesViewModel by entriesViewModel {

    private val endingBalance = SimpleObjectProperty(Money.ZERO)
    private val clearedBalance = SimpleObjectProperty(Money.ZERO)
    private val difference = SimpleObjectProperty(Money.ZERO)

    val startingBalanceProperty: ReadOnlyObjectProperty<Money> = SimpleObjectProperty(statement.startingBalance)
    val endingBalanceProperty: ReadOnlyObjectProperty<Money> = endingBalance
    val clearedBalanceProperty: ReadOnlyObjectProperty<Money> = clearedBalance
    val differenceProperty: ReadOnlyObjectProperty<Money> = difference

    private val transactionsConsumer = Consumer<AsyncObject<List<FXAccountEntry>>> { async ->
        if (async is AsyncObject.Complete) {
            clearedBalance.value = async.value
                    .filter { it.statusProperty.value == TransactionStatus.CLEARED }
                    .fold(Money.ZERO) { acc, t -> acc + t.amountProperty.value }
        }
    }

    private val transactionsListener = ChangeListener<AsyncObject<List<FXAccountEntry>>> { _, _, newValue ->
        transactionsConsumer.accept(newValue)
    }

    private val weakTransactionsListener = WeakChangeListener(transactionsListener)

    init {
        transactionsConsumer.accept(transactionsProperty.value)

        val endingBalanceCalculator = Callable { statement.startingBalance!! + clearedBalance.value }
        val endingBalanceBinding = Bindings.createObjectBinding(endingBalanceCalculator, clearedBalanceProperty)
        endingBalance.bind(endingBalanceBinding)

        val differenceCalculator = Callable { statement.endingBalance!! - endingBalance.value }
        val differenceBinding = Bindings.createObjectBinding(differenceCalculator, endingBalanceProperty)
        difference.bind(differenceBinding)

        transactionsProperty.addListener(weakTransactionsListener)
    }
}
