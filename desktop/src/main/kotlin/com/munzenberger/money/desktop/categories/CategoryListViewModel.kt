package com.munzenberger.money.desktop.categories

import androidx.lifecycle.ViewModel
import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<CategoryListUiState> = repositoryController.resultFlow { it.categories }
        .map { result ->
            result.fold(
                onSuccess = { categories -> CategoryListUiState.Content(categories = categories) },
                onFailure = { CategoryListUiState.Error },
            )
        }
}
