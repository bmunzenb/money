package com.munzenberger.money.desktop.rail

import com.munzenberger.money.desktop.navigation.Route
import money.shared.generated.resources.Res
import money.shared.generated.resources.accounts_button_title
import money.shared.generated.resources.categories_button_title
import money.shared.generated.resources.payees_button_title
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
    val route: Route,
    val label: StringResource,
) {
    Accounts(Route.AccountList, Res.string.accounts_button_title),
    Categories(Route.CategoryList, Res.string.categories_button_title),
    Payees(Route.PayeeList, Res.string.payees_button_title);

    companion object {
        fun of(route: Route?): TopLevelDestination? = entries.firstOrNull { it.route == route }
    }
}
