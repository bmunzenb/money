package com.munzenberger.money.desktop.components

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.munzenberger.money.shared.theme.MoneyTheme

/**
 * A [LazyColumn] with a [VerticalScrollbar] along its end edge. [contentPadding] is applied inside the
 * scrolling area, so the scrollbar sits in the end padding rather than over the content.
 */
@Composable
fun ScrollableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: LazyListScope.() -> Unit,
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            content = content,
        )

        ThemedVerticalScrollbar(adapter = rememberScrollbarAdapter(state))
    }
}

/**
 * A vertically scrolling [Column] with a [VerticalScrollbar] along its end edge. [contentPadding] is
 * applied inside the scrolling area, so the scrollbar sits in the end padding rather than over the
 * content.
 */
@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    state: ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state)
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )

        ThemedVerticalScrollbar(adapter = rememberScrollbarAdapter(state))
    }
}

@Composable
private fun BoxScope.ThemedVerticalScrollbar(adapter: ScrollbarAdapter) {
    VerticalScrollbar(
        adapter = adapter,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight(),
        style = themedScrollbarStyle(),
    )
}

/** The default scrollbar style is tinted black, which disappears in dark mode; tint it from the theme. */
@Composable
private fun themedScrollbarStyle(): ScrollbarStyle {
    val onSurface = MoneyTheme.colorScheme.onSurface
    return LocalScrollbarStyle.current.copy(
        unhoverColor = onSurface.copy(alpha = 0.12f),
        hoverColor = onSurface.copy(alpha = 0.50f),
    )
}
