package com.munzenberger.money.desktop.components

import com.munzenberger.money.presentation.state.LoadState
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.StackPane

/**
 * Shows [content] once what it shows has loaded, a progress indicator in its place while it loads, and [errorMessage]
 * in its place if it fails to.
 */
class LoadStateContainer(private val content: Node, errorMessage: String) : StackPane() {
    private val loadingProgress = ProgressIndicator()

    private val loadError = errorLabel(errorMessage)

    init {
        alignment = Pos.TOP_LEFT
        setAlignment(loadingProgress, Pos.CENTER)
        children.setAll(content, loadingProgress, loadError)
    }

    /** Shows the content, the progress indicator or the error message, for [state]. */
    fun setLoadState(state: LoadState<*>) {
        content.setShown(state is LoadState.Loaded)
        loadingProgress.setShown(state is LoadState.Loading)
        loadError.setShown(state is LoadState.Error)
    }
}
