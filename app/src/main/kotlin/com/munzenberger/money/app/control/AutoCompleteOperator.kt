package com.munzenberger.money.app.control

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import java.util.*
import java.util.function.UnaryOperator

class AutoCompleteOperator<T>(items: ObservableList<T>, converter: StringConverter<T>) : UnaryOperator<TextFormatter.Change> {

    private val strings: SortedSet<String> = items.map { converter.toString(it) }.toSortedSet(compareBy({ it.length }, { it }))

    init {
        // update the set if the item list changes
        items.addListener(ListChangeListener<T> { change ->
            while (change.next()) {
                change.removed.forEach { strings.remove(converter.toString(it)) }
                change.addedSubList.forEach { strings.add(converter.toString(it)) }
            }
        })
    }

    override fun apply(change: TextFormatter.Change): TextFormatter.Change {

        if (change.text.isEmpty()) {
            // text was deleted, don't auto-complete
            return change
        }

        val newText = change.controlNewText

        strings.forEach {
            when {
                it == newText ->
                    // perfect match, don't auto-complete
                    return change

                it.startsWith(newText) -> {

                    val value = it.substring(newText.length-1)
                    val newAnchor = it.length
                    val newCaretPosition = change.caretPosition

                    change.text = value
                    change.selectRange(newAnchor, newCaretPosition)

                    return change
                }
            }
        }

        return change
    }
}
