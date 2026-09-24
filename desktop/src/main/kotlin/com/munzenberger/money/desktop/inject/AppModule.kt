package com.munzenberger.money.desktop.inject

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.desktop.AppViewModel
import com.munzenberger.money.desktop.MenuBarViewModel
import com.munzenberger.money.desktop.accounts.AccountListViewModel
import com.munzenberger.money.desktop.navigation.Navigator
import com.munzenberger.money.desktop.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MoneyRepositoryController() }
    single { Navigator() }

    viewModel { AppViewModel(get(), get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { AccountListViewModel(get()) }
    viewModel { MenuBarViewModel(get()) }
}
