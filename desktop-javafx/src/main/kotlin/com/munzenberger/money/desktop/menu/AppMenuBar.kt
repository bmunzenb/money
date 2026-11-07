package com.munzenberger.money.desktop.menu

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.menu.AppMenuBarUiState
import com.munzenberger.money.presentation.menu.AppMenuBarViewModel
import javafx.application.Platform
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem

/**
 * The app's menu bar. It uses the system menu bar where the OS has one (the screen-top bar on macOS), and is shown at
 * the top of the window elsewhere, such as on Windows and Linux.
 */
class AppMenuBar(scope: ScreenScope) : MenuBar() {
    private val viewModel: AppMenuBarViewModel = scope.viewModel()

    private val closeRepositoryMenuItem = MenuItem(strings.getString("close_repository_menu_item_title")).apply {
        setOnAction { viewModel.onCloseRepositoryClick() }
    }

    // Exits through Platform.exit(), so MoneyApplication.stop() saves the window bounds and closes the database.
    private val exitMenuItem = MenuItem(strings.getString("exit_menu_item_title")).apply {
        setOnAction { Platform.exit() }
    }

    // macOS already has Quit in the app menu, so File has no Exit there.
    private val fileMenu = Menu(strings.getString("file_menu_title")).apply {
        items += closeRepositoryMenuItem
        if (!isMacOs) items.addAll(SeparatorMenuItem(), exitMenuItem)
    }

    init {
        isUseSystemMenuBar = true
        menus.setAll(fileMenu)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: AppMenuBarUiState) {
        closeRepositoryMenuItem.isDisable = !state.canCloseRepository
    }

    private companion object {
        val isMacOs = System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)
    }
}
