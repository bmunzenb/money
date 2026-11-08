package com.munzenberger.money.desktop.components

import com.munzenberger.money.desktop.theme.Spacing
import javafx.css.PseudoClass
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

private const val SMALL_PROGRESS_SIZE = 16.0

private val ERROR_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("error")

/** A label styled as an error message, which wraps to fit its width. */
fun errorLabel(message: String? = null) = Label(message).apply {
    styleClass += "error-message"
    isWrapText = true
}

/** A progress indicator small enough to go beside a field. */
fun smallProgressIndicator() = ProgressIndicator().apply {
    setPrefSize(SMALL_PROGRESS_SIZE, SMALL_PROGRESS_SIZE)
    setMaxSize(SMALL_PROGRESS_SIZE, SMALL_PROGRESS_SIZE)
}

/** Puts [progress] after [this], which takes the rest of the width. */
fun Node.withProgress(progress: Node) = HBox(Spacing.CONTROL_SPACING, this, progress).apply {
    alignment = Pos.CENTER_LEFT
    HBox.setHgrow(this@withProgress, Priority.ALWAYS)
}

/** Puts [error] below [this]. */
fun Node.withError(error: Node) = VBox(Spacing.TINY, this, error)

/**
 * Sets this control's text, unless it already has it. Setting the text the field already has would move the caret, so a
 * field's own change isn't set back on it.
 */
fun TextInputControl.setTextIfChanged(text: String) {
    if (this.text != text) this.text = text
}

/** Shows this node as invalid, or not, through the stylesheet's `error` pseudo-class. */
fun Node.setError(isError: Boolean) = pseudoClassStateChanged(ERROR_PSEUDO_CLASS, isError)
