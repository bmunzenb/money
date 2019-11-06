package com.munzenberger.money.app

import com.munzenberger.money.app.property.*
import com.munzenberger.money.core.*
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
    val notValidProperty: ReadOnlyBooleanProperty = notValid
    val saveStatusProperty: ReadOnlyAsyncStatusProperty = saveStatus

    fun start(database: MoneyDatabase, account: Account) {

        this.database = database
        this.account = account

        accountNameProperty.value = account.name

        accountTypes.subscribeTo(AccountType.getAllForCategories(
                database,
                AccountType.Category.ASSETS,
                AccountType.Category.LIABILITIES))

        selectedAccountTypeProperty.value = account.accountType

        accountNumberProperty.value = account.number

        banks.subscribeTo(Bank.getAll(database).map {
            it.sortedBy { b -> b.name }
        })

        selectedBankProperty.value = account.bank

        notValid.bind(accountNameProperty.isEmpty.or(selectedAccountTypeProperty.isNull))
    }

    fun save() {

        account.apply {
            name = accountNameProperty.value
            accountType = selectedAccountTypeProperty.value
            number = accountNumberProperty.value
            bank = selectedBankProperty.value
        }

        val save = when (account.identity) {
            null -> {
                // if the account doesn't already exist, create it via
                // a category to support transfers between accounts
                val category = Category()
                category.account = account
                category.save(database)
            }
            else -> account.save(database)
        }

        saveStatus.subscribeTo(save)
    }
}
