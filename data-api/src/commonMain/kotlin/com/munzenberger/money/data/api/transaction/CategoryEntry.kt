package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.category.CategoryId
import kotlin.uuid.Uuid

@JvmInline
value class CategoryEntryId(val value: Uuid = Uuid.random())

data class CategoryEntry(
    val id: CategoryEntryId = CategoryEntryId(),
    override val transactionId: TransactionId,
    val categoryId: CategoryId,
    override val amount: Money,
    override val memo: String? = null,
    override val orderInTransaction: Int,
) : TransactionEntry
