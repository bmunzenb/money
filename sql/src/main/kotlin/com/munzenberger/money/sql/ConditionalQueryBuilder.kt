package com.munzenberger.money.sql

abstract class ConditionalQueryBuilder<T> {
    private var where: Condition? = null

    fun where(condition: Condition): T {
        this.where = condition
        return instance()
    }

    protected abstract fun instance(): T

    fun build() = build(where)

    abstract fun build(where: Condition?): Query
}
