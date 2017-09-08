package frangsierra.kotlinfirechat.common.rx

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


interface SubscriptionTracker {
    /**
     * Clear Subscriptions.
     */
    fun cancelSubscriptions(): Unit

    /**
     * Start tracking a disposable.
     */
    fun <T : Disposable> T.track(): T
}

class DefaultSubscriptionTracker : SubscriptionTracker {
    private val disposables = CompositeDisposable()
    override fun cancelSubscriptions() = disposables.clear()
    override fun <T : Disposable> T.track(): T {
        disposables.add(this)
        return this
    }
}