package com.munzenberger.money.presentation.categories

import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.presentation.state.LoadState

data class CategoryListUiState(
    val categories: LoadState<List<Category>> = LoadState.Loading,
)
