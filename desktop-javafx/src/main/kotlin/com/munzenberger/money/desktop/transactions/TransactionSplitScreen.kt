package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * Edits the split of the transaction in the form at [Route.TransactionSplit.form]. It shares the form's ViewModel, so
 * it edits the form's own entries: Done keeps the changes, and Cancel or closing the window puts the entries back as
 * they were when it opened.
 */
class TransactionSplitScreen(scope: ScreenScope, route: Route.TransactionSplit) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel(of = route.form)

    private val doneButton = Button(strings.getString("done_button_title")).apply {
        isDefaultButton = true
        ButtonBar.setButtonData(this, ButtonBar.ButtonData.OK_DONE)
        setOnAction { viewModel.onSplitDoneClick() }
    }

    private val cancelButton = Button(strings.getString("cancel_button_title")).apply {
        isCancelButton = true
        ButtonBar.setButtonData(this, ButtonBar.ButtonData.CANCEL_CLOSE)
        setOnAction { viewModel.onSplitCancelClick() }
    }

    // ButtonBar orders the buttons for the platform, as the forms' footers do.
    private val footer = HBox(Spacing.CONTROL_SPACING, ButtonBar().apply { buttons.setAll(doneButton, cancelButton) })
        .apply { alignment = Pos.CENTER_RIGHT }

    init {
        padding = Insets(Spacing.SCREEN_PADDING)
        children.setAll(footer)
    }

    /** Cancels the split, as Cancel does. */
    fun onCloseRequest() = viewModel.onSplitCancelClick()
}
