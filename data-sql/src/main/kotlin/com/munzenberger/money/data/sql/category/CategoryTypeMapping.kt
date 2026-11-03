package com.munzenberger.money.data.sql.category

import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId

/** Maps a `CategoryType` row, or the type columns joined into another row. */
internal fun categoryType(id: Long, value: String) = CategoryType(
    id = CategoryTypeId(id),
    value = CategoryTypeConstant.valueOf(value),
)
