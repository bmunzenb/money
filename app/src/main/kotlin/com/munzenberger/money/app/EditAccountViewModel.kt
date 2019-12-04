package com.munzenberger.money.app

import com.munzenberger.money.app.model.getForCategories
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.ReadOnlyAsyncStatusProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncStatusProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.Category
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.rx.observableGetAll
import com.munzenberger.money.core.rx.observableSave
import com.munzenberger.money.core.rx.sortedBy
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

        accountTypes.subscribeTo(AccountType.getForCategories(
                database,
                AccountType.Category.ASSETS,
                AccountType.Category.LIABILITIES))

        selectedAccountTypeProperty.value = account.accountType

        accountNumberProperty.value = account.number

        banks.subscribeTo(Bank.observableGetAll(database).sortedBy { it.name })

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

        val save = when (account.identity) {
            null -> {
                // if the account doesn't already exist, create it via
                // a category to support transfers between accounts
                val category = Category()
                category.account = account
                category.observableSave(database)
            }
            else -> account.observableSave(database)
        }

        saveStatus.subscribeTo(save)
    }
}
