package com.munzenberger.money.desktop.transactions

import com.munzenberger.money.desktop.components.AccountBalanceView
import com.munzenberger.money.desktop.navigation.ScreenScope
import com.munzenberger.money.desktop.strings
import com.munzenberger.money.desktop.theme.Spacing
import com.munzenberger.money.presentation.navigation.Route
import com.munzenberger.money.presentation.state.LoadState
import com.munzenberger.money.presentation.state.loadedOrEmpty
import com.munzenberger.money.presentation.transactions.TransactionDetailsUiState
import com.munzenberger.money.presentation.transactions.TransactionDetailsViewModel
import com.munzenberger.money.presentation.transactions.total
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlin.uuid.Uuid

/**
 * Edits the split of the transaction in the form at [Route.TransactionSplit.form]. It shares the form's ViewModel, so
 * it edits the form's own entries: Done keeps the changes, and Cancel or closing the window puts the entries back as
 * they were when it opened.
 */
class TransactionSplitScreen(scope: ScreenScope, route: Route.TransactionSplit) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: TransactionDetailsViewModel = scope.viewModel(of = route.form)

    private val table = TransactionSplitTable().apply {
        setVgrow(this, Priority.ALWAYS)
        selectionModel.selectedIndexProperty().addListener { _, _, _ -> updateEntryButtons() }
    }

    private val addButton = entryButton("split_add_button_title") { viewModel.onAddEntryClick() }

    private val removeButton = entryButton("split_remove_button_title") {
        selectedKey()?.let(viewModel::onRemoveEntryClick)
    }

    private val moveUpButton = entryButton("split_move_up_button_title") {
        selectedKey()?.let(viewModel::onMoveEntryUpClick)
    }

    private val moveDownButton = entryButton("split_move_down_button_title") {
        selectedKey()?.let(viewModel::onMoveEntryDownClick)
    }

    // The buttons acting on the table's rows, in a column to its right, each as wide as the widest.
    private val entryButtons = VBox(
        Spacing.CONTROL_SPACING,
        addButton,
        removeButton,
        moveUpButton,
        moveDownButton,
    )

    // The sum of the rows' amounts, right below the table and in bold, like the account list's total. It's unavailable
    // while any row's amount isn't a valid amount.
    private val totalBalance = AccountBalanceView(strings.getString("split_total_label")).apply {
        styleClass += "total-balance"
    }

    // The table with the total under it, so the total lines up with the table's right edge rather than the buttons'.
    private val tableColumn = VBox(Spacing.CONTROL_SPACING, table, totalBalance).apply {
        HBox.setHgrow(this, Priority.ALWAYS)
    }

    // Fills the space above the footer as the dialog is resized.
    private val tableRow = HBox(Spacing.CONTROL_SPACING, tableColumn, entryButtons).apply {
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
        children.setAll(tableRow, footer)
        updateEntryButtons()
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: TransactionDetailsUiState) {
        table.show(state.entries.loadedOrEmpty)
        addButton.isDisable = state.entries !is LoadState.Loaded
        updateEntryButtons()
        totalBalance.render(
            when (val entries = state.entries) {
                LoadState.Loading -> LoadState.Loading
                LoadState.Error -> LoadState.Error
                is LoadState.Loaded -> entries.value.total()?.let { LoadState.Loaded(it) } ?: LoadState.Error
            },
        )
    }

    private fun selectedKey(): Uuid? = table.selectionModel.selectedItem?.key

    /**
     * Enables the buttons that act on the selected row while one is selected, and the move buttons only while the row
     * can move that way.
     */
    private fun updateEntryButtons() {
        val index = table.selectionModel.selectedIndex
        val hasSelection = index >= 0
        removeButton.isDisable = !hasSelection
        moveUpButton.isDisable = !hasSelection || index == 0
        moveDownButton.isDisable = !hasSelection || index == table.items.lastIndex
    }

    /** Cancels the split, as Cancel does. */
    fun onCloseRequest() = viewModel.onSplitCancelClick()

    /** A button in the column beside the table, titled from [titleKey], that stretches to the column's width. */
    private fun entryButton(titleKey: String, onAction: () -> Unit) = Button(strings.getString(titleKey)).apply {
        maxWidth = Double.MAX_VALUE
        setOnAction { onAction() }
    }

    private companion object {
        const val PREF_WIDTH = 700.0
        const val PREF_HEIGHT = 400.0
    }
}
