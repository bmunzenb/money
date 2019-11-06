package com.munzenberger.money.app

import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.Payee
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import java.time.LocalDate

class EditTransactionViewModel : AutoCloseable {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()
    private val categories = SimpleAsyncObjectProperty<List<Category>>()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val selectedTypeProperty = SimpleObjectProperty<TransactionType?>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<Category>> = categories
    val selectedCategoryProperty = SimpleObjectProperty<Category?>()

    fun start(database: MoneyDatabase, account: Account) {

        selectedAccountProperty.value = account

        accounts.subscribeTo(Account.getAll(database).map {
            it.sortedBy { a -> a.name }
        })

        types.addAll(TransactionType.getTypes())

        dateProperty.value = LocalDate.now()

        payees.subscribeTo(Payee.getAll(database).map {
            it.sortedBy { p -> p.name }
        })

        // TODO: sort the categories
        categories.subscribeTo(Category.getAll(database))
    }

    override fun close() {
        // do nothing
    }
}
