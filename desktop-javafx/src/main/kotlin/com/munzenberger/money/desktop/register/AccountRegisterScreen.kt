package com.munzenberger.money.desktop.register

import com.munzenberger.money.desktop.components.ScreenHeading
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.register.AccountRegisterViewModel
import javafx.geometry.Insets
import javafx.scene.layout.VBox
import org.koin.core.parameter.parametersOf

class AccountRegisterScreen(scope: ScreenScope, route: Route.AccountRegister) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: AccountRegisterViewModel = scope.viewModel { parametersOf(route) }

    private val heading = ScreenHeading("Account Register Screen")

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.addAll(heading)
    }
}
