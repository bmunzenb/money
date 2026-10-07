package com.munzenberger.money.desktop.inject

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.account.CreateAccountUseCase
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import com.munzenberger.money.desktop.AppViewModel
import com.munzenberger.money.desktop.accounts.AccountListViewModel
import com.munzenberger.money.desktop.accounts.NewAccountViewModel
import com.munzenberger.money.desktop.categories.CategoryListViewModel
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.payees.PayeeListViewModel
import com.munzenberger.money.desktop.rail.AppNavigationRailViewModel
import com.munzenberger.money.desktop.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MoneyRepositoryController(connectorFactory = { file -> SqlMoneyRepositoryConnector(file) }) }
    single { Navigator() }

    factory { CreateAccountUseCase(get()) }

    viewModel { AppViewModel(get(), get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { AccountListViewModel(get(), get()) }
    viewModel { NewAccountViewModel(get(), get(), get()) }
    viewModel { CategoryListViewModel(get()) }
    viewModel { PayeeListViewModel(get()) }
    viewModel { AppNavigationRailViewModel(get()) }
}
