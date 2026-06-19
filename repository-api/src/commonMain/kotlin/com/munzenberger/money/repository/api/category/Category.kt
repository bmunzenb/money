package com.munzenberger.money.repository.api.category

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@ExperimentalUuidApi
value class CategoryId(val value: Uuid = Uuid.random())

data class Category(
    @OptIn(ExperimentalUuidApi::class)
    val id: CategoryId = CategoryId(),
    val name: String,
    @OptIn(ExperimentalUuidApi::class)
    val parent: CategoryId? = null,
    val type: CategoryType,
    val memo: String? = null,
)
