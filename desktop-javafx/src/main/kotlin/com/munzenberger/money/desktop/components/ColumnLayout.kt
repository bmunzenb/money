package com.munzenberger.money.desktop.components

import com.munzenberger.money.data.api.preferences.MoneyPreferences
import com.munzenberger.money.data.api.preferences.PreferenceKey
import javafx.animation.Animation
import javafx.animation.PauseTransition
import javafx.beans.InvalidationListener
import javafx.scene.control.TreeTableView
import javafx.util.Duration
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

private const val SAVE_DELAY_MILLIS = 500.0

/** A table column's saved state, identified by the column's id. */
@Serializable
data class ColumnLayout(val id: String, val width: Double)

/** Creates a key for a table's column layout: its columns in order, each with its width. */
fun columnLayoutKey(name: String) =
    PreferenceKey(name, emptyList(), ListSerializer(ColumnLayout.serializer()))

/**
 * Restores the order and widths of this table's columns from [preferences] under [key], and saves them there as they
 * change. Each column is stored by its id, so a column without a saved layout keeps its default width and goes after
 * the saved columns.
 */
fun <S> TreeTableView<S>.saveColumnLayout(preferences: MoneyPreferences, key: PreferenceKey<List<ColumnLayout>>) {
    val saved = preferences[key]
    val savedIndexes = saved.withIndex().associate { (index, layout) -> layout.id to index }
    val savedWidths = saved.associate { it.id to it.width }
    columns.setAll(columns.sortedBy { savedIndexes[it.id] ?: Int.MAX_VALUE })
    columns.forEach { column -> savedWidths[column.id]?.let { column.prefWidth = it } }

    // Saves once the layout stops changing, e.g. at the end of a drag, rather than on every change.
    val saveDelay = PauseTransition(Duration.millis(SAVE_DELAY_MILLIS))
    val save = { preferences[key] = columns.map { ColumnLayout(it.id, it.width) } }
    saveDelay.setOnFinished { save() }

    val listener = InvalidationListener { saveDelay.playFromStart() }
    columns.addListener(listener)
    columns.forEach { it.widthProperty().addListener(listener) }

    // Saves a pending change when the table leaves the scene, e.g. when another screen is shown or the app closes.
    sceneProperty().addListener { _, _, scene ->
        if (scene == null && saveDelay.status == Animation.Status.RUNNING) {
            saveDelay.stop()
            save()
        }
    }
}
