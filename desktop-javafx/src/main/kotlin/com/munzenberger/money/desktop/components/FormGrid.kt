package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.theme.Spacing
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority

/**
 * A form's fields, each in a row beside its label. The labels' column is as wide as its widest label, never shrinking
 * to cut one off. The fields take the rest.
 */
class FormGrid : GridPane() {
    init {
        hgap = Spacing.MEDIUM
        vgap = Spacing.MEDIUM
        columnConstraints.setAll(
            ColumnConstraints().apply {
                minWidth = USE_PREF_SIZE
                prefWidth = USE_COMPUTED_SIZE
                maxWidth = USE_PREF_SIZE
                hgrow = Priority.NEVER
            },
            ColumnConstraints().apply { hgrow = Priority.ALWAYS },
        )
    }

    /**
     * Adds a row with the [label] for [field], and [fieldNode], which holds the field and anything that goes with it.
     * The default label padding lines up the label's text with the text inside a control such as a text field; a field
     * that's plain text, with no padding of its own, takes no label padding instead.
     */
    fun addField(
        label: String,
        field: Node,
        fieldNode: Node = field,
        labelPadding: Insets = Insets(LABEL_TOP_PADDING, 0.0, 0.0, 0.0),
    ) {
        val row = rowCount
        val fieldLabel = Label(label).apply { labelFor = field }
        setValignment(fieldLabel, VPos.TOP)
        fieldLabel.padding = labelPadding
        add(fieldLabel, 0, row)
        add(fieldNode, 1, row)
    }

    companion object {
        private const val LABEL_TOP_PADDING = 4.0
    }
}
