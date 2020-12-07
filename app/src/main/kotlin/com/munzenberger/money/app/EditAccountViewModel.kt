package com.munzenberger.money.app

import com.munzenberger.money.app.model.getForAccounts
import com.munzenberger.money.app.property.AsyncObject
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.app.property.subscribe
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.Single
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class EditAccountViewModel {

    private lateinit var database: MoneyDatabase
    private lateinit var account: Account

    private val accountTypes = SimpleAsyncObjectProperty<List<AccountType>>()
    private val banks = SimpleAsyncObjectProperty<List<Bank>>()
    private val notValid = SimpleBooleanProperty()
    private val saveStatus = SimpleAsyncStatusProperty()

    val accountNameProperty = SimpleStringProperty()
    val accountTypesProperty: ReadOnlyAsyncObjectProperty<List<AccountType>> = accountTypes
    val selectedAccountTypeProperty = SimpleObjectProperty<AccountType?>()
    val accountNumberProperty = SimpleStringProperty()
    val banksProperty: ReadOnlyAsyncObjectProperty<List<Bank>> = banks
    val selectedBankProperty = SimpleObjectProperty<Bank?>()
    val initialBalanceProperty = SimpleObjectProperty<Money?>()
    val notValidProperty: ReadOnlyBooleanProperty = notValid
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus

    fun start(database: MoneyDatabase, account: Account) {

        this.database = database
        this.account = account

        accountNameProperty.value = account.name

        Single.fromCallable { AccountType.getForAccounts(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe(accountTypes)

        selectedAccountTypeProperty.value = account.accountType

        accountNumberProperty.value = account.number

        Single.fromCallable { Bank.getAll(database).sortedBy { it.name } }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe(banks)

        selectedBankProperty.value = account.bank

        initialBalanceProperty.value = account.initialBalance

        notValid.bind(accountNameProperty.isEmpty.or(selectedAccountTypeProperty.isNull))
    }

    fun save() {

        account.apply {
            name = accountNameProperty.value
            accountType = selectedAccountTypeProperty.value
            number = accountNumberProperty.value
            bank = selectedBankProperty.value
            initialBalance = initialBalanceProperty.value
        }

        Single.fromCallable { account.save(database) }
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .doOnSubscribe { saveStatus.value = AsyncObject.Executing() }
                .subscribe(saveStatus)
    }
}
