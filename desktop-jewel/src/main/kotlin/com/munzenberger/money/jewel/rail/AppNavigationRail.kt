package com.munzenberger.money.jewel.rail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.jewel.theme.PreviewThemed
import com.munzenberger.money.presentation.rail.AppNavigationRailViewModel
import com.munzenberger.money.presentation.rail.TopLevelDestination
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.ui.component.SimpleListItem
import org.koin.compose.viewmodel.koinViewModel

/**
 * The app's top-level navigation: a list of the [TopLevelDestination]s with their icons and labels, like
 * the section list on the left of the IDE's Settings dialog. Jewel has no navigation rail component.
 */
@Composable
fun AppNavigationRail(viewModel: AppNavigationRailViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    AppNavigationRailContent(
        selected = state.selected,
        onDestinationClick = viewModel::onDestinationClick,
    )
}

@Composable
private fun AppNavigationRailContent(
    selected: TopLevelDestination?,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(RailWidth)
            .fillMaxHeight()
            .padding(8.dp)
            .selectableGroup(),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            SimpleListItem(
                text = stringResource(destination.label),
                selected = destination == selected,
                icon = destination.icon,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = destination == selected,
                        interactionSource = null,
                        indication = null,
                        role = Role.Tab,
                        onClick = { onDestinationClick(destination) },
                    ),
            )
        }
    }
}

private val RailWidth = 200.dp

@Preview
@Composable
private fun AppNavigationRailPreview() {
    PreviewThemed {
        AppNavigationRailContent(
            selected = TopLevelDestination.Accounts,
            onDestinationClick = {},
        )
    }
}
