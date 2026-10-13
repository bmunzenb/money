package com.munzenberger.money.desktop.rail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector
import com.munzenberger.money.presentation.rail.TopLevelDestination

val TopLevelDestination.icon: ImageVector
    get() = when (this) {
        TopLevelDestination.Accounts -> Icons.Filled.AccountBalanceWallet
        TopLevelDestination.Categories -> Icons.Filled.Category
        TopLevelDestination.Payees -> Icons.Filled.People
    }
