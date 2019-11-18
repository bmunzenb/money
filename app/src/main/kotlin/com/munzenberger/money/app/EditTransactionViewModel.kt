package com.munzenberger.money.app

import com.munzenberger.money.app.model.*
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.core.*
import io.reactivex.Completable
import javafx.beans.property.*
import javafx.collections.FXCollections
import java.time.LocalDate

class EditTransactionViewModel : AutoCloseable {

    private val accounts = SimpleAsyncObjectProperty<List<Account>>()
    private val payees = SimpleAsyncObjectProperty<List<Payee>>()
    private val types = FXCollections.observableArrayList<TransactionType>()
    private val typeDisabled = SimpleBooleanProperty(true)
    private val categories = SimpleAsyncObjectProperty<List<DelayedCategory>>()
    private val categoryDisabled = SimpleBooleanProperty(true)
    private val splitDisabled = SimpleBooleanProperty(true)
    private val amountDisabled = SimpleBooleanProperty(true)
    private val saveStatus = SimpleAsyncStatusProperty()
    private val notValid = SimpleBooleanProperty()

    val accountsProperty: ReadOnlyAsyncObjectProperty<List<Account>> = accounts
    val selectedAccountProperty = SimpleObjectProperty<Account?>()
    val typesProperty: ReadOnlyListProperty<TransactionType> = SimpleListProperty(types)
    val typeDisabledProperty: ReadOnlyBooleanProperty = typeDisabled
    val selectedTypeProperty = SimpleObjectProperty<TransactionType?>()
    val dateProperty = SimpleObjectProperty<LocalDate>()
    val payeesProperty: ReadOnlyAsyncObjectProperty<List<Payee>> = payees
    val selectedPayeeProperty = SimpleObjectProperty<Payee?>()
    val categoriesProperty: ReadOnlyAsyncObjectProperty<List<DelayedCategory>> = categories
    val categoryDisabledProperty: ReadOnlyBooleanProperty = categoryDisabled
    val selectedCategoryProperty = SimpleObjectProperty<DelayedCategory?>()
    val splitDisabledProperty: ReadOnlyBooleanProperty = splitDisabled
    val amountProperty = SimpleObjectProperty<Money>()
    val amountDisabledProperty: ReadOnlyBooleanProperty = amountDisabled
    val memoProperty = SimpleStringProperty()
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus
    val notValidProperty: ReadOnlyBooleanProperty = notValid

    private val transfersViewModel = EditTransfersViewModel(
            typeDisabled = typeDisabled,
            selectedType = selectedTypeProperty,
            categoryDisabled = categoryDisabled,
            selectedCategory = selectedCategoryProperty,
            amountDisabled = amountDisabled,
            amount = amountProperty)

    private lateinit var database: MoneyDatabase
    private lateinit var transaction: Transaction

    init {
        notValid.bind(selectedAccountProperty.isNull
                .or(selectedTypeProperty.isNull)
                .or(dateProperty.isNull)
                .or(selectedCategoryProperty.isNull)
                .or(amountProperty.isNull))
    }

    fun start(database: MoneyDatabase, account: Account, schedulers: SchedulerProvider = SchedulerProvider.Default) {

        this.database = database
        this.transaction = Transaction()

        selectedAccountProperty.value = account

        accounts.subscribeTo(Account.getAssetsAndLiabilities(database))

        types.addAll(TransactionType.getTypes())

        dateProperty.value = LocalDate.now()

        payees.subscribeTo(Payee.getAllSorted(database))

        categories.subscribeTo(Category.getAll(database).map {
            it.map { c -> RealCategory(c) }
        })

        transaction.getTransfers(database)
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.main)
                .doOnSuccess { splitDisabled.value = false }
                .subscribe(transfersViewModel::setTransfers)
    }

    fun save() {

        transaction.apply {
            account = selectedAccountProperty.value
            date = dateProperty.value.toDate()
            payee = selectedPayeeProperty.value
            memo = memoProperty.value
        }

        val save = database.transaction { tx ->
            Completable.concatArray(
                    transaction.save(tx),
                    transfersViewModel.save(transaction, tx))
        }

        saveStatus.subscribeTo(save)
    }

    override fun close() {
        // do nothing
    }
}
