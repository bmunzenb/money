package com.munzenberger.money.desktop.rail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector
import com.munzenberger.money.desktop.navigation.Route
import money.shared.generated.resources.Res
import money.shared.generated.resources.accounts_button_title
import money.shared.generated.resources.categories_button_title
import money.shared.generated.resources.payees_button_title
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
    val route: Route,
    val icon: ImageVector,
    val label: StringResource,
) {
    Accounts(Route.AccountList, Icons.Filled.AccountBalanceWallet, Res.string.accounts_button_title),
    Categories(Route.CategoryList, Icons.Filled.Category, Res.string.categories_button_title),
    Payees(Route.PayeeList, Icons.Filled.People, Res.string.payees_button_title);

    companion object {
        fun of(route: Route?): TopLevelDestination? = entries.firstOrNull { it.route == route }
    }
}
