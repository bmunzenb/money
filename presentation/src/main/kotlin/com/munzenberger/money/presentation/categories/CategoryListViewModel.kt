package com.munzenberger.money.presentation.categories

import androidx.lifecycle.ViewModel
import com.munzenberger.money.presentation.database.MoneyRepositoryController
import com.munzenberger.money.presentation.database.resultFlow
import com.munzenberger.money.presentation.state.toLoadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryListViewModel(
    repositoryController: MoneyRepositoryController
) : ViewModel() {

    val state: Flow<CategoryListUiState> = repositoryController.resultFlow { it.categories }
        .map { CategoryListUiState(categories = it.toLoadState()) }

    fun onAddCategoryClick() {
        // Navigation to an add category screen will go here once that route exists.
    }
}
