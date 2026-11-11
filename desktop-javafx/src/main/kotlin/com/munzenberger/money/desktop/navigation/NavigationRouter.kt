package com.munzenberger.money.desktop.navigation

import com.munzenberger.money.desktop.accounts.AccountSettingsScreen
import com.munzenberger.money.desktop.accounts.AccountListScreen
import com.munzenberger.money.desktop.register.AccountRegisterScreen
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.welcome.WelcomeScreen
import com.munzenberger.money.presentation.navigation.Route
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.StackPane

// Exhaustive, so adding a Route fails to compile until it's mapped to a screen here, shown in the content pane or in
// a dialog.
val navigationRouter: (route: Route, scope: ScreenScope) -> Destination = { route, scope ->
    when (route) {
        Route.Welcome -> Destination.Screen(WelcomeScreen(scope))
        Route.AccountList -> Destination.Screen(AccountListScreen(scope))
        is Route.NewAccount -> Destination.Dialog(
            AccountSettingsScreen(scope, route),
            strings.getString("new_account_title"),
        )
        is Route.AccountSettings -> Destination.Dialog(
            AccountSettingsScreen(scope, route),
            strings.getString("account_settings_title"),
        )
        is Route.AccountRegister -> Destination.Screen(AccountRegisterScreen(scope, route))
        Route.CategoryList -> Destination.Screen(placeholderScreen(route))
        Route.PayeeList -> Destination.Screen(placeholderScreen(route))
        is Route.TransactionFormRoute -> Destination.Screen(placeholderScreen(route))
        is Route.TransferDetails -> Destination.Screen(placeholderScreen(route))
    }
}

// Stands in for a route's screen until it has one, showing the route so it's clear where navigation went.
private fun placeholderScreen(route: Route): Parent = StackPane(Label(route.toString()))
