package com.munzenberger.money.presentation.rail

import com.munzenberger.money.presentation.navigation.Route

enum class TopLevelDestination(val route: Route) {
    Accounts(Route.AccountList),
    Categories(Route.CategoryList),
    Payees(Route.PayeeList),
    Portfolio(Route.Portfolio),
    Schedule(Route.Schedule);

    companion object {
        fun of(route: Route?): TopLevelDestination? = entries.firstOrNull { it.route == route }
    }
}
