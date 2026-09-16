package com.munzenberger.money.desktop.accounts

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountListScreen(@Suppress("UnusedParameter") viewModel: AccountListViewModel = koinViewModel()) {
    AccountListScreenContent()
}

@Composable
private fun AccountListScreenContent() {
    Text(
        text = stringResource(Res.string.account_list_title),
        style = MoneyTheme.typography.headlineMedium
    )
}

@Preview
@Composable
private fun AccountListScreenPreview() {
    PreviewThemed {
        AccountListScreenContent()
    }
}
