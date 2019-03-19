package com.munzenberger.money.app

import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class EditAccountViewModel {

    private lateinit var database: MoneyDatabase
    private lateinit var account: Account

    private val accountTypes = SimpleAsyncObjectProperty<List<AccountType>>()
    private val banks = SimpleAsyncObjectProperty<List<Bank>>()

    val accountNameProperty = SimpleStringProperty()
    val accountTypesProperty: ReadOnlyAsyncObjectProperty<List<AccountType>> = accountTypes
    val selectedAccountTypeProperty = SimpleObjectProperty<AccountType?>()
    val accountNumberProperty = SimpleStringProperty()
    val banksProperty: ReadOnlyAsyncObjectProperty<List<Bank>> = banks
    val selectedBankProperty = SimpleObjectProperty<Bank?>()
    val notValidProperty = SimpleBooleanProperty()

    fun start(database: MoneyDatabase, account: Account) {

        this.database = database
        this.account = account

        accountNameProperty.value = account.name

        accountTypes.subscribe(AccountType.getAllForCategories(
                database,
                AccountType.Category.ASSETS,
                AccountType.Category.LIABILITIES))

        selectedAccountTypeProperty.value = account.accountType

        accountNumberProperty.value = account.number

        banks.subscribe(Bank.getAll(database))

        selectedBankProperty.value = account.bank

        notValidProperty.bind(accountNameProperty.isEmpty.or(selectedAccountTypeProperty.isNull))
    }
}
