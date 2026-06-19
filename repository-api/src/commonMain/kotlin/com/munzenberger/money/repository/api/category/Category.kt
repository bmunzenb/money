package com.munzenberger.money.repository.api.category

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class CategoryId(val value: Uuid = Uuid.random())

@OptIn(ExperimentalUuidApi::class)
data class Category(
    val id: CategoryId = CategoryId(),
    val name: String,
    val parentId: CategoryId? = null,
    val type: CategoryType,
    val memo: String? = null,
)
