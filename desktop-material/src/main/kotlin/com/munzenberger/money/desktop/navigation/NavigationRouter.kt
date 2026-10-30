package com.munzenberger.money.desktop.navigation

import androidx.navigation3.runtime.entryProvider
import com.munzenberger.money.desktop.accounts.AccountSettingsScreen
import com.munzenberger.money.desktop.accounts.AccountListScreen
import com.munzenberger.money.desktop.categories.CategoryListScreen
import com.munzenberger.money.desktop.payees.PayeeListScreen
import com.munzenberger.money.desktop.welcome.WelcomeScreen
import com.munzenberger.money.presentation.navigation.Route

val navigationRouter = entryProvider<Route> {
    entry<Route.Welcome> {
        WelcomeScreen()
    }

    entry<Route.AccountList> {
        AccountListScreen()
    }

    entry<Route.NewAccount> {
        AccountSettingsScreen()
    }

    entry<Route.AccountSettings> {
        AccountSettingsScreen(accountId = it.accountId)
    }

    entry<Route.CategoryList> {
        CategoryListScreen()
    }

    entry<Route.PayeeList> {
        PayeeListScreen()
    }
}
