package com.munzenberger.money.data.api.category

interface CategoryWriter {
    fun add(category: Category)

    fun update(category: Category)

    fun removeById(categoryId: CategoryId)
}

fun CategoryWriter.remove(category: Category) {
    removeById(category.id)
}
