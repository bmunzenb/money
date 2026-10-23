package com.munzenberger.money.jewel.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import com.munzenberger.money.jewel.accounts.AccountFormScreen
import com.munzenberger.money.jewel.accounts.AccountListScreen
import com.munzenberger.money.jewel.welcome.WelcomeScreen
import com.munzenberger.money.presentation.navigation.Route
import org.jetbrains.jewel.ui.component.Text

val navigationRouter = entryProvider<Route> {
    entry<Route.Welcome> {
        WelcomeScreen()
    }

    entry<Route.AccountList> {
        AccountListScreen()
    }

    entry<Route.NewAccount> {
        AccountFormScreen()
    }

    entry<Route.EditAccount> {
        AccountFormScreen(accountId = it.accountId)
    }

    entry<Route.CategoryList> {
        RoutePlaceholder(it)
    }

    entry<Route.PayeeList> {
        RoutePlaceholder(it)
    }
}

/** Stands in for a screen that hasn't been built in this UI yet. */
@Composable
private fun RoutePlaceholder(route: Route) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = route.toString())
    }
}
