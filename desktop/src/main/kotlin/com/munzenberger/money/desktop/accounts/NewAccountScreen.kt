package com.munzenberger.money.desktop.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.desktop.components.DetailScreenHeader
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.new_account_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewAccountScreen(viewModel: NewAccountViewModel = koinViewModel()) {
    NewAccountScreenContent(onBackClick = viewModel::onBackClick)
}

@Composable
private fun NewAccountScreenContent(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        DetailScreenHeader(
            title = stringResource(Res.string.new_account_title),
            onBackClick = onBackClick,
        )
    }
}

@Preview
@Composable
private fun NewAccountScreenPreview() {
    PreviewThemed {
        NewAccountScreenContent(onBackClick = {})
    }
}
