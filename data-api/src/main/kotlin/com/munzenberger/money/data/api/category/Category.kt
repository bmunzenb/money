package com.munzenberger.money.data.api.category

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class CategoryId @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid = Uuid.generateV7())

data class Category(
    val id: CategoryId = CategoryId(),
    val name: String,
    val parentId: CategoryId? = null,
    val type: CategoryType,
    val memo: String? = null,
)
