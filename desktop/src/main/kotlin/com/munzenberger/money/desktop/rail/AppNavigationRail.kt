package com.munzenberger.money.desktop.rail

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.munzenberger.money.shared.theme.PreviewThemed
import kotlinx.coroutines.launch
import money.shared.generated.resources.Res
import money.shared.generated.resources.collapse_navigation_rail_button_description
import money.shared.generated.resources.expand_navigation_rail_button_description
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
    railState: WideNavigationRailState = rememberWideNavigationRailState(),
) {
    val scope = rememberCoroutineScope()
    val isExpanded = railState.targetValue == WideNavigationRailValue.Expanded

    WideNavigationRail(
        state = railState,
        colors = WideNavigationRailDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        header = {
            IconButton(
                onClick = { scope.launch { railState.toggle() } },
                modifier = Modifier.padding(start = HeaderButtonStartPadding),
            ) {
                if (isExpanded) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = stringResource(Res.string.collapse_navigation_rail_button_description),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(Res.string.expand_navigation_rail_button_description),
                    )
                }
            }
        },
    ) {
        TopLevelDestination.entries.forEach { destination ->
            WideNavigationRailItem(
                selected = destination == selected,
                onClick = { onDestinationClick(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { Text(text = stringResource(destination.label)) },
                railExpanded = isExpanded,
            )
        }
    }
}

// Rail items inset their icons 36dp from the rail's start edge (20dp item padding + 16dp indicator
// padding) in both states; IconButton centers its 24dp icon in a 48dp touch target, so its icon starts
// 12dp in. This padding lines the toggle's icon up with the item icons.
private val HeaderButtonStartPadding = 24.dp

@Preview
@Composable
private fun AppNavigationRailCollapsedPreview() {
    PreviewThemed {
        AppNavigationRailContent(
            selected = TopLevelDestination.Accounts,
            onDestinationClick = {},
        )
    }
}

@Preview
@Composable
private fun AppNavigationRailExpandedPreview() {
    PreviewThemed {
        AppNavigationRailContent(
            selected = TopLevelDestination.Accounts,
            onDestinationClick = {},
            railState = rememberWideNavigationRailState(WideNavigationRailValue.Expanded),
        )
    }
}
