package com.munzenberger.money.desktop.payees

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.payee.Payee
import com.munzenberger.money.desktop.components.ListScreenHeader
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.add_payee_button_title
import money.shared.generated.resources.payee_list_empty_message
import money.shared.generated.resources.payee_list_error_message
import money.shared.generated.resources.payee_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PayeeListScreen(viewModel: PayeeListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = PayeeListUiState.Loading)

    PayeeListScreenContent(
        state = state,
        onAddPayeeClick = viewModel::onAddPayeeClick,
    )
}

@Composable
private fun PayeeListScreenContent(
    state: PayeeListUiState,
    onAddPayeeClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListScreenHeader(
            title = stringResource(Res.string.payee_list_title),
            actionLabel = stringResource(Res.string.add_payee_button_title),
            onActionClick = onAddPayeeClick,
        )

        Box(modifier = Modifier.weight(1f)) {
            PayeeListBody(state = state)
        }
    }
}

@Composable
private fun PayeeListBody(state: PayeeListUiState) {
    when (state) {
        is PayeeListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is PayeeListUiState.Error -> {
            Text(text = stringResource(Res.string.payee_list_error_message))
        }
        is PayeeListUiState.Content -> {
            if (state.payees.isEmpty()) {
                Text(text = stringResource(Res.string.payee_list_empty_message))
            } else {
                LazyColumn {
                    items(state.payees, key = { it.id.value }) { payee ->
                        Text(text = payee.name)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PayeeListScreenLoadingPreview() {
    PreviewThemed {
        PayeeListScreenContent(state = PayeeListUiState.Loading, onAddPayeeClick = {})
    }
}

@Preview
@Composable
private fun PayeeListScreenErrorPreview() {
    PreviewThemed {
        PayeeListScreenContent(state = PayeeListUiState.Error, onAddPayeeClick = {})
    }
}

@Preview
@Composable
private fun PayeeListScreenWithPayeesPreview() {
    PreviewThemed {
        PayeeListScreenContent(
            state = PayeeListUiState.Content(
                payees = listOf(
                    Payee(name = "Grocery Store"),
                    Payee(name = "Electric Company"),
                )
            ),
            onAddPayeeClick = {},
        )
    }
}

@Preview
@Composable
private fun PayeeListScreenEmptyPreview() {
    PreviewThemed {
        PayeeListScreenContent(
            state = PayeeListUiState.Content(payees = emptyList()),
            onAddPayeeClick = {},
        )
    }
}
