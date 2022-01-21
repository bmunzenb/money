package com.munzenberger.money.app.database

import java.util.Collections
import java.util.concurrent.Executor

interface ObservableDatabase {
    fun subscribeOnUpdate(executor: Executor, block: Runnable): Subscription
}

interface Subscription {
    fun cancel()
}

class CompositeSubscription : Subscription {

    private val subscriptions = mutableListOf<Subscription>()

    fun add(subscription: Subscription) {
        synchronized(subscriptions) {
            subscriptions.add(subscription)
        }
    }

    override fun cancel() {
        synchronized(subscriptions) {
            subscriptions.forEach { it.cancel() }
            subscriptions.clear()
        }
    }
}

class ObservableDatabaseImpl() : ObservableDatabase {

    private val onUpdateSubscribers = Collections.synchronizedList(mutableListOf<Pair<Executor, Runnable>>())

    override fun subscribeOnUpdate(executor: Executor, block: Runnable): Subscription {
        val pair = executor to block
        onUpdateSubscribers.add(pair)
        executor.execute(block)
        return object : Subscription {
            override fun cancel() {
                onUpdateSubscribers.remove(pair)
            }
        }
    }

    fun fireOnUpdate() {
        synchronized(onUpdateSubscribers) {
            onUpdateSubscribers.forEach {
                val (executor, block) = it
                executor.execute(block)
            }
        }
    }
}
