package com.munzenberger.money.desktop.rail

import androidx.compose.runtime.Composable
import com.munzenberger.money.presentation.rail.TopLevelDestination
import money.desktop_material.generated.resources.Res
import money.desktop_material.generated.resources.accounts_button_title
import money.desktop_material.generated.resources.categories_button_title
import money.desktop_material.generated.resources.payees_button_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopLevelDestination.label(): String = stringResource(
    when (this) {
        TopLevelDestination.Accounts -> Res.string.accounts_button_title
        TopLevelDestination.Categories -> Res.string.categories_button_title
        TopLevelDestination.Payees -> Res.string.payees_button_title
    }
)
