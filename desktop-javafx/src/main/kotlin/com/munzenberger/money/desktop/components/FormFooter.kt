package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.theme.Spacing
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

/**
 * A form's [buttons], after a progress indicator for while the form is busy, under a separator. It's as wide as
 * [fields], with the same end padding, so the separator and buttons line up with the end of the fields however wide
 * the grid is.
 */
class FormFooter(fields: FormGrid, vararg buttons: Button) : VBox(Spacing.CONTROL_SPACING) {
    private val progress = smallProgressIndicator()

    // ButtonBar orders the buttons by their data for the platform, e.g. Cancel before Save on macOS and after it on
    // Windows.
    private val buttonBar = ButtonBar().apply {
        this.buttons.setAll(buttons.toList())
    }

    private val buttonRow = HBox(Spacing.CONTROL_SPACING, progress, buttonBar).apply {
        alignment = Pos.CENTER_RIGHT
    }

    init {
        padding = FormGrid.END_PADDING
        maxWidthProperty().bind(fields.widthProperty())
        children.setAll(Separator(), buttonRow)
    }

    /** Shows or hides the progress indicator. */
    fun setProgressShown(shown: Boolean) = progress.setShown(shown)
}
