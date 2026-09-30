package com.munzenberger.money.desktop.categories

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
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.category_list_empty_message
import money.shared.generated.resources.category_list_error_message
import money.shared.generated.resources.category_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryListScreen(viewModel: CategoryListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = CategoryListUiState.Loading)

    CategoryListScreenContent(state = state)
}

@Composable
private fun CategoryListScreenContent(state: CategoryListUiState) {
    Column {
        Text(
            text = stringResource(Res.string.category_list_title),
            style = MoneyTheme.typography.headlineMedium
        )

        when (state) {
            is CategoryListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CategoryListUiState.Error -> {
                Text(text = stringResource(Res.string.category_list_error_message))
            }
            is CategoryListUiState.Content -> {
                if (state.categories.isEmpty()) {
                    Text(text = stringResource(Res.string.category_list_empty_message))
                } else {
                    LazyColumn {
                        items(state.categories, key = { it.id.value }) { category ->
                            Text(text = category.name)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoryListScreenLoadingPreview() {
    PreviewThemed {
        CategoryListScreenContent(state = CategoryListUiState.Loading)
    }
}

@Preview
@Composable
private fun CategoryListScreenErrorPreview() {
    PreviewThemed {
        CategoryListScreenContent(state = CategoryListUiState.Error)
    }
}

@Preview
@Composable
private fun CategoryListScreenWithCategoriesPreview() {
    PreviewThemed {
        CategoryListScreenContent(
            state = CategoryListUiState.Content(
                categories = listOf(
                    Category(name = "Groceries", type = previewCategoryType),
                    Category(name = "Rent", type = previewCategoryType),
                )
            )
        )
    }
}

@Preview
@Composable
private fun CategoryListScreenEmptyPreview() {
    PreviewThemed {
        CategoryListScreenContent(state = CategoryListUiState.Content(categories = emptyList()))
    }
}

private val previewCategoryType = CategoryType(
    id = CategoryTypeId(1),
    value = CategoryTypeConstant.Expense,
)
