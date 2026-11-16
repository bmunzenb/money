package com.munzenberger.money.core.category

import com.munzenberger.money.data.api.category.Category

data class CategoryWithParent(
    val category: Category,
    val parent: Category?
)
