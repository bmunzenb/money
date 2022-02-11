package com.munzenberger.money.app

import com.munzenberger.money.app.concurrent.Executors
import com.munzenberger.money.app.concurrent.setValueAsync
import com.munzenberger.money.app.property.ReadOnlyAsyncObjectProperty
import com.munzenberger.money.app.property.SimpleAsyncObjectProperty
import com.munzenberger.money.core.Account
import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.Bank
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task

class EditAccountViewModel {

    private lateinit var database: MoneyDatabase
    private lateinit var account: Account

    private val accountTypes = SimpleAsyncObjectProperty<List<AccountType>>()
    private val banks = SimpleAsyncObjectProperty<List<Bank>>()
    private val notValid = SimpleBooleanProperty()
    private val isOperationInProgress = SimpleBooleanProperty(false)

    val accountNameProperty = SimpleStringProperty()
    val accountTypesProperty: ReadOnlyAsyncObjectProperty<List<AccountType>> = accountTypes
    val selectedAccountTypeProperty = SimpleObjectProperty<AccountType?>()
    val accountNumberProperty = SimpleStringProperty()
    val banksProperty: ReadOnlyAsyncObjectProperty<List<Bank>> = banks
    val selectedBankProperty = SimpleObjectProperty<Bank?>()
    val initialBalanceProperty = SimpleObjectProperty<Money?>()
    val isOperationInProgressProperty: ReadOnlyBooleanProperty = isOperationInProgress
    val notValidProperty: ReadOnlyBooleanProperty = notValid

    fun start(database: MoneyDatabase, account: Account) {

        this.database = database
        this.account = account

        accountNameProperty.value = account.name

        accountTypes.setValueAsync { AccountType.getAll(database) }

        selectedAccountTypeProperty.value = account.accountType

        accountNumberProperty.value = account.number

        banks.setValueAsync { Bank.getAll(database).sortedBy { it.name } }

        selectedBankProperty.value = account.bank

        initialBalanceProperty.value = account.initialBalance

        notValid.bind(accountNameProperty.isEmpty.or(selectedAccountTypeProperty.isNull))
    }

    fun save(block: (Throwable?) -> Unit) {

        val task = object : Task<Unit>() {

            override fun call() {
                account.apply {

                    name = accountNameProperty.value
                    accountType = selectedAccountTypeProperty.value
                    number = accountNumberProperty.value
                    bank = selectedBankProperty.value
                    initialBalance = initialBalanceProperty.value

                    save(database)
                }
            }

            override fun succeeded() {
                block.invoke(null)
            }

            override fun failed() {
                block.invoke(exception)
            }
        }

        isOperationInProgress.bind(task.runningProperty())

        Executors.SINGLE.execute(task)
    }
}
