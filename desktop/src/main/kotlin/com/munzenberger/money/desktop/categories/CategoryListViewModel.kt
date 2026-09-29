package com.munzenberger.money.desktop.categories

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<CategoryListUiState> = repositoryController.flow { it.categories }
        .map { categories -> CategoryListUiState.Content(categories = categories) }
}
