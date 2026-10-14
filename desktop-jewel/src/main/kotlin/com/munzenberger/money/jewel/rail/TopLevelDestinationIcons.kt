package com.munzenberger.money.jewel.rail

import com.munzenberger.money.presentation.rail.TopLevelDestination
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys

val TopLevelDestination.icon: IconKey
    get() = when (this) {
        TopLevelDestination.Accounts -> AllIconsKeys.Nodes.DataTables
        TopLevelDestination.Categories -> AllIconsKeys.Nodes.Tag
        TopLevelDestination.Payees -> AllIconsKeys.General.User
    }
