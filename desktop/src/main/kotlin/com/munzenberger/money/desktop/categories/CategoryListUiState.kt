package com.munzenberger.money.desktop.categories

import com.munzenberger.money.data.api.category.Category

sealed interface CategoryListUiState {
    data object Loading : CategoryListUiState
    data object Error : CategoryListUiState
    data class Content(val categories: List<Category> = emptyList()) : CategoryListUiState
}
