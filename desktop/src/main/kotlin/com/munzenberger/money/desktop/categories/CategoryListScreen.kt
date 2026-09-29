package com.munzenberger.money.desktop.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.shared.theme.MoneyTheme
import com.munzenberger.money.shared.theme.PreviewThemed
import money.shared.generated.resources.Res
import money.shared.generated.resources.category_list_empty_message
import money.shared.generated.resources.category_list_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryListScreen(viewModel: CategoryListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = CategoryListUiState())

    CategoryListScreenContent(categories = state.categories)
}

@Composable
private fun CategoryListScreenContent(categories: List<Category>) {
    Column {
        Text(
            text = stringResource(Res.string.category_list_title),
            style = MoneyTheme.typography.headlineMedium
        )

        if (categories.isEmpty()) {
            Text(text = stringResource(Res.string.category_list_empty_message))
        } else {
            LazyColumn {
                items(categories, key = { it.id.value }) { category ->
                    Text(text = category.name)
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoryListScreenWithCategoriesPreview() {
    PreviewThemed {
        CategoryListScreenContent(
            categories = listOf(
                Category(name = "Groceries", type = previewCategoryType),
                Category(name = "Rent", type = previewCategoryType),
            )
        )
    }
}

@Preview
@Composable
private fun CategoryListScreenEmptyPreview() {
    PreviewThemed {
        CategoryListScreenContent(categories = emptyList())
    }
}

private val previewCategoryType = CategoryType(
    id = CategoryTypeId(1),
    value = CategoryTypeConstant.Expense,
)
