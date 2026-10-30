package com.munzenberger.money.presentation.inject

import com.munzenberger.money.core.account.CreateAccountUseCase
import com.munzenberger.money.core.account.GetAccountGroupsUseCase
import com.munzenberger.money.core.account.UpdateAccountUseCase
import com.munzenberger.money.core.demo.PopulateDemoDataUseCase
import com.munzenberger.money.data.sql.SqlMoneyRepositoryConnector
import com.munzenberger.money.presentation.AppViewModel
import com.munzenberger.money.presentation.accounts.AccountSettingsViewModel
import com.munzenberger.money.presentation.accounts.AccountListViewModel
import com.munzenberger.money.presentation.categories.CategoryListViewModel
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.navigation.Navigator
import com.munzenberger.money.presentation.payees.PayeeListViewModel
import com.munzenberger.money.presentation.preferences.UserPreferences
import com.munzenberger.money.presentation.rail.AppNavigationRailViewModel
import com.munzenberger.money.presentation.toolbar.AppToolbarViewModel
import com.munzenberger.money.presentation.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    single {
        MoneyRepositoryController(
            fileConnectorFactory = { file -> SqlMoneyRepositoryConnector(file) },
            demoConnectorFactory = { SqlMoneyRepositoryConnector.inMemory() },
            populateDemoData = get(),
        )
    }
    single { Navigator() }
    single { UserPreferences() }

    factory { CreateAccountUseCase() }
    factory { GetAccountGroupsUseCase() }
    factory { UpdateAccountUseCase() }
    factory { PopulateDemoDataUseCase() }

    viewModel { AppViewModel(get(), get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { AccountListViewModel(get(), get(), get(), get()) }
    viewModel { params -> AccountSettingsViewModel(params.getOrNull(), get(), get(), get(), get()) }
    viewModel { CategoryListViewModel(get()) }
    viewModel { PayeeListViewModel(get()) }
    viewModel { AppNavigationRailViewModel(get()) }
    viewModel { AppToolbarViewModel(get()) }
}
