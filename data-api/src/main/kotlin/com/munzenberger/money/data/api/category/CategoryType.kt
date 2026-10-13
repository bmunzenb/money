package com.munzenberger.money.data.api.category

import kotlinx.serialization.Serializable

enum class CategoryTypeConstant {
    Income, Expense
}

@Serializable
@JvmInline
value class CategoryTypeId(val value: Long)

data class CategoryType(
    val id: CategoryTypeId,
    val value: CategoryTypeConstant
)
