package com.munzenberger.money.desktop.categories

import com.munzenberger.money.data.api.category.Category

data class CategoryListUiState(
    val categories: List<Category> = emptyList()
)
