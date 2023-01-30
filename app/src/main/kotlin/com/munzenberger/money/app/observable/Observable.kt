package com.munzenberger.money.app.observable

import java.util.*
import java.util.concurrent.Executor

interface Observable {
    fun subscribe(executor: Executor, block: Runnable): Subscription
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

class ObservableImpl : Observable {

    private val subscribers = Collections.synchronizedList(mutableListOf<Pair<Executor, Runnable>>())

    override fun subscribe(executor: Executor, block: Runnable): Subscription {
        val pair = executor to block
        subscribers.add(pair)
        executor.execute(block)
        return object : Subscription {
            override fun cancel() {
                subscribers.remove(pair)
            }
        }
    }

    fun onNext() {
        synchronized(subscribers) {
            subscribers.forEach { (executor, block) -> executor.execute(block) }
        }
    }
}
