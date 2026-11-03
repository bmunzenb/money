package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.theme.Spacing
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.layout.HBox

/**
 * A form's [buttons], after a progress indicator for while the form is busy. It's as wide as [fields], with the same
 * end padding, so the buttons line up with the end of the fields however wide the grid is.
 */
class FormFooter(fields: FormGrid, vararg buttons: Button) : HBox(Spacing.CONTROL_SPACING) {
    private val progress = smallProgressIndicator()

    // ButtonBar orders the buttons by their data for the platform, e.g. Cancel before Save on macOS and after it on
    // Windows.
    private val buttonBar = ButtonBar().apply {
        this.buttons.setAll(buttons.toList())
    }

    init {
        alignment = Pos.CENTER_RIGHT
        padding = FormGrid.END_PADDING
        maxWidthProperty().bind(fields.widthProperty())
        children.setAll(progress, buttonBar)
    }

    /** Shows or hides the progress indicator. */
    fun setProgressShown(shown: Boolean) = progress.setShown(shown)
}
