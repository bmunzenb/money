package com.munzenberger.money.desktop.components

import com.munzenberger.money.data.api.preferences.MoneyPreferences
import com.munzenberger.money.data.api.preferences.PreferenceKey
import javafx.animation.Animation
import javafx.animation.PauseTransition
import javafx.beans.InvalidationListener
import javafx.scene.control.TreeTableView
import javafx.util.Duration
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

private const val SAVE_DELAY_MILLIS = 500.0

/** Creates a key for a table's column widths, stored by column id. */
fun columnWidthsKey(name: String) =
    PreferenceKey(name, emptyMap(), MapSerializer(String.serializer(), Double.serializer()))

/**
 * Restores the widths of this table's columns from [preferences] under [key], and saves them there as they change.
 * Each column is stored by its id, so a column without a saved width keeps its default width.
 */
fun TreeTableView<*>.saveColumnWidths(preferences: MoneyPreferences, key: PreferenceKey<Map<String, Double>>) {
    val saved = preferences[key]
    columns.forEach { column -> saved[column.id]?.let { column.prefWidth = it } }

    // Saves once the widths stop changing, e.g. at the end of a drag, rather than on every change.
    val saveDelay = PauseTransition(Duration.millis(SAVE_DELAY_MILLIS))
    val save = { preferences[key] = columns.associate { it.id to it.width } }
    saveDelay.setOnFinished { save() }

    val listener = InvalidationListener { saveDelay.playFromStart() }
    columns.forEach { it.widthProperty().addListener(listener) }

    // Saves a pending change when the table leaves the scene, e.g. when another screen is shown or the app closes.
    sceneProperty().addListener { _, _, scene ->
        if (scene == null && saveDelay.status == Animation.Status.RUNNING) {
            saveDelay.stop()
            save()
        }
    }
}
