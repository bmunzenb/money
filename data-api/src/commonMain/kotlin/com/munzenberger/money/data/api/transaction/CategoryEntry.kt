package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.category.CategoryId
import kotlin.uuid.Uuid

@JvmInline
value class CategoryEntryId(val value: Uuid = Uuid.random())

data class CategoryEntry(
    val id: CategoryEntryId = CategoryEntryId(),
    val transactionId: TransactionId,
    val categoryId: CategoryId,
    val amount: Money,
    val memo: String? = null,
    val orderInTransaction: Int,
)
