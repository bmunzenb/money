package com.munzenberger.money.repository.api.category

enum class CategoryTypeConstant {
    Income, Expense
}

@JvmInline
value class CategoryTypeId(val value: Long)

data class CategoryType(
    val id: CategoryTypeId,
    val value: CategoryTypeConstant
)
