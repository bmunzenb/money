package com.munzenberger.money.data.api.category

import com.munzenberger.money.data.api.EntityNotFoundException

interface CategoryWriter {
    fun add(category: Category)

    /** Updates the stored category with [category]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(category: Category)

    fun removeById(categoryId: CategoryId)
}

fun CategoryWriter.remove(category: Category) {
    removeById(category.id)
}
