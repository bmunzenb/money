package com.munzenberger.money.desktop.payees

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.payee_list_empty_message
import money.shared.generated.resources.payee_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PayeeListScreen(viewModel: PayeeListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = PayeeListUiState())

    PayeeListScreenContent(payees = state.payees)
}

@Composable
private fun PayeeListScreenContent(payees: List<Payee>) {
    Column {
        Text(
            text = stringResource(Res.string.payee_list_title),
            style = MoneyTheme.typography.headlineMedium
        )

        if (payees.isEmpty()) {
            Text(text = stringResource(Res.string.payee_list_empty_message))
        } else {
            LazyColumn {
                items(payees, key = { it.id.value }) { payee ->
                    Text(text = payee.name)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PayeeListScreenWithPayeesPreview() {
    PreviewThemed {
        PayeeListScreenContent(
            payees = listOf(
                Payee(name = "Grocery Store"),
                Payee(name = "Electric Company"),
            )
        )
    }
}

@Preview
@Composable
private fun PayeeListScreenEmptyPreview() {
    PreviewThemed {
        PayeeListScreenContent(payees = emptyList())
    }
}
