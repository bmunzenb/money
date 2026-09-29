package com.munzenberger.money.desktop.toolbar

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.navigate_back_button_description
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppToolBar(viewModel: AppToolBarViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    AppToolBarContent(
        isBackEnabled = state.isBackEnabled,
        onBackClick = viewModel::onBackClick,
    )
}

@Composable
private fun AppToolBarContent(isBackEnabled: Boolean, onBackClick: () -> Unit) {
    Row {
        IconButton(onClick = onBackClick, enabled = isBackEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.navigate_back_button_description),
            )
        }
    }
}

@Preview
@Composable
private fun AppToolBarBackEnabledPreview() {
    PreviewThemed {
        AppToolBarContent(isBackEnabled = true, onBackClick = {})
    }
}

@Preview
@Composable
private fun AppToolBarBackDisabledPreview() {
    PreviewThemed {
        AppToolBarContent(isBackEnabled = false, onBackClick = {})
    }
}
