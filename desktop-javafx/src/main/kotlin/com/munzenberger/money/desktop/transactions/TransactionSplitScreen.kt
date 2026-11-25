package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.loadedOrEmpty
import com.munzenberger.money.presentation.transactions.TransactionCategoryFormatter
import com.munzenberger.money.presentation.transactions.TransactionDetailsUiState
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Edits the split of the transaction in the form at [Route.TransactionSplit.form]. It shares the form's ViewModel, so
 * it edits the form's own entries: Done keeps the changes, and Cancel or closing the window puts the entries back as
 * they were when it opened.
 */
class TransactionSplitScreen(scope: ScreenScope, route: Route.TransactionSplit) :
    VBox(Spacing.CONTROL_SPACING),
    KoinComponent {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel(of = route.form)

    // Formats the categories as the ViewModel matches them, so text picked from a row's list selects that category.
    private val categoryFormatter: TransactionCategoryFormatter by inject()

    // Fills the space above the footer as the dialog is resized.
    private val table = TransactionSplitTable(onCategoryChange = viewModel::onEntryCategoryChange).apply {
        setVgrow(this, Priority.ALWAYS)
    }

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
        setPrefSize(PREF_WIDTH, PREF_HEIGHT)
        children.setAll(table, footer)
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: TransactionDetailsUiState) {
        table.showCategories(state.categories.loadedOrEmpty.map(categoryFormatter::format))
        table.show(state.entries.loadedOrEmpty)
    }

    /** Cancels the split, as Cancel does. */
    fun onCloseRequest() = viewModel.onSplitCancelClick()

    private companion object {
        const val PREF_WIDTH = 600.0
        const val PREF_HEIGHT = 400.0
    }
}
