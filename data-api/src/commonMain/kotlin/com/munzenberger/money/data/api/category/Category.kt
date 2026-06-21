package com.munzenberger.money.data.api.category

import kotlin.uuid.Uuid

@JvmInline
value class CategoryId(val value: Uuid = Uuid.random())

data class Category(
    val id: CategoryId = CategoryId(),
    val name: String,
    val parentId: CategoryId? = null,
    val type: CategoryType,
    val memo: String? = null,
)
