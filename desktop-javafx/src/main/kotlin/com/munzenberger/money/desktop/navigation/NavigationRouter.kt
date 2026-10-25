package com.munzenberger.money.desktop.navigation

import com.munzenberger.money.desktop.welcome.WelcomeScreen
import com.munzenberger.money.presentation.navigation.Route
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.StackPane

// Exhaustive, so adding a Route fails to compile until it's mapped to a screen here.
val navigationRouter: (route: Route, scope: ScreenScope) -> Node = { route, scope ->
    when (route) {
        Route.Welcome -> WelcomeScreen(scope.viewModel())
        Route.AccountList -> placeholderScreen(route)
        Route.NewAccount -> placeholderScreen(route)
        is Route.EditAccount -> placeholderScreen(route)
        Route.CategoryList -> placeholderScreen(route)
        Route.PayeeList -> placeholderScreen(route)
    }
}

// Stands in for a route's screen until it has one, showing the route so it's clear where navigation went.
private fun placeholderScreen(route: Route): Node = StackPane(Label(route.toString()))
