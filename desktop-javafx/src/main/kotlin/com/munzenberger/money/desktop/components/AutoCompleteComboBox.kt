package com.munzenberger.money.desktop.components

import javafx.css.PseudoClass
import javafx.scene.control.ComboBox
import javafx.scene.control.TextFormatter

/**
 * An editable combo box that completes what the user types to the first item it's the start of, ignoring case. The
 * completed part is selected, so typing on replaces it and completes again, and deleting it leaves what was typed.
 *
 * It uses the editor's text formatter, so the editor can't have another.
 */
class AutoCompleteComboBox<T> : ComboBox<T>() {
    /**
     * Whether the user can only read the text: the editor can't be edited and the list can't be shown, while the text
     * keeps its normal look, unlike a disabled combo box's. The arrow is dimmed through the `read-only` pseudo-class.
     */
    var isReadOnly: Boolean = false
        set(value) {
            field = value
            editor.isEditable = !value
            pseudoClassStateChanged(READ_ONLY_PSEUDO_CLASS, value)
            if (value) hide()
        }

    init {
        isEditable = true
        editor.textFormatter = TextFormatter<String>(::complete)
    }

    // Covers every way of showing the list, such as clicking the arrow or pressing Alt+Down.
    override fun show() {
        if (!isReadOnly) super.show()
    }

    private fun complete(change: TextFormatter.Change): TextFormatter.Change {
        // Only text the user adds is completed, not text they delete. Text set on the editor, such as an item picked
        // from the list or the screen filling the field in, comes with the caret at the start, so nothing is typed.
        val typed = change.controlNewText.substring(0, change.caretPosition)
        if (change.isAdded && typed.isNotEmpty()) {
            completionOf(typed)?.let { completion ->
                change.setRange(0, change.controlText.length)
                change.text = completion
                change.selectRange(completion.length, typed.length)
            }
        }
        return change
    }

    // An item the user has typed in full wins over a longer one it's the start of, so they can still choose it.
    private fun completionOf(typed: String): String? {
        val itemTexts = items.map(converter::toString)
        return itemTexts.firstOrNull { it.equals(typed, ignoreCase = true) }
            ?: itemTexts.firstOrNull { it.startsWith(typed, ignoreCase = true) }
    }

    private companion object {
        val READ_ONLY_PSEUDO_CLASS: PseudoClass = PseudoClass.getPseudoClass("read-only")
    }
}
