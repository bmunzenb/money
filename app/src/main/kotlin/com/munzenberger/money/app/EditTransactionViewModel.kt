package com.munzenberger.money.app

import com.munzenberger.money.app.model.DelayedCategory
import com.munzenberger.money.app.model.RealCategory
import com.munzenberger.money.app.model.getAllSorted
import com.munzenberger.money.app.model.getAssetsAndLiabilities
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.*
import javafx.beans.property.*
import javafx.collections.FXCollections
import java.time.LocalDate

class EditTransactionViewModel : AutoCloseable {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()
    private val categories = SimpleAsyncObjectProperty<List<DelayedCategory>>()
    private val notValid = SimpleBooleanProperty()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val selectedTypeProperty = SimpleObjectProperty<TransactionType?>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<DelayedCategory>> = categories
    val selectedCategoryProperty = SimpleObjectProperty<DelayedCategory?>()
    val amountProperty = SimpleObjectProperty<Money>()
    val memoProperty = SimpleStringProperty()
    val notValidProperty: ReadOnlyBooleanProperty = notValid

    init {
        notValid.bind(selectedAccountProperty.isNull
                .or(selectedTypeProperty.isNull)
                .or(dateProperty.isNull)
                .or(selectedCategoryProperty.isNull)
                .or(amountProperty.isNull))
    }

    fun start(database: MoneyDatabase, account: Account) {

        selectedAccountProperty.value = account

        accounts.subscribeTo(Account.getAssetsAndLiabilities(database))

        types.addAll(TransactionType.getTypes())

        dateProperty.value = LocalDate.now()

        payees.subscribeTo(Payee.getAllSorted(database))

        categories.subscribeTo(Category.getAll(database).map {
            it.map { c -> RealCategory(c) }
        })
    }

    override fun close() {
        // do nothing
    }
}
