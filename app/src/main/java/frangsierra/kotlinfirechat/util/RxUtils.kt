package frangsierra.kotlinfirechat.util

import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import mini.TypedTask
import java.util.concurrent.TimeUnit

/** Apply the mapping function if object is not null. */
inline fun <T, U> Flowable<T>.view(crossinline fn: (T) -> U?): Flowable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Flowable.empty()
        else Flowable.just(mapped)
    }.distinctUntilChanged()
}

/** Apply the mapping function if object is not null. */
inline fun <T, U> Observable<T>.view(crossinline fn: (T) -> U?): Observable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Observable.empty()
        else Observable.just(mapped)
    }.distinctUntilChanged()
}

/** Take the first element that matches the filter function. */
inline fun <T> Observable<T>.filterOne(crossinline fn: (T) -> Boolean): Maybe<T> {
    return filter { fn(it) }.take(1).singleElement()
}

/** Take the first element that matches the filter function. */
inline fun <T> Flowable<T>.filterOne(crossinline fn: (T) -> Boolean): Maybe<T> {
    return filter { fn(it) }.take(1).singleElement()
}

inline fun <S, D, T : TypedTask<D>> Flowable<S>.onNextTerminalState(
    crossinline mapFn: (S) -> T,
    crossinline success: (S) -> Unit = {},
    crossinline failure: (Throwable) -> Unit) {

    filterOne { mapFn(it).isTerminal() }
        .subscribe {
            val task = mapFn(it)
            if (task.isSuccessful()) {
                success(it)
            } else {
                failure(task.error!!)
            }
        }
}

fun Completable.defaultTimeout() = timeout(20, TimeUnit.SECONDS)
fun <T> Maybe<T>.defaultTimeout() = timeout(20, TimeUnit.SECONDS)
fun <T> Single<T>.defaultTimeout() = timeout(20, TimeUnit.SECONDS)
fun <T> Observable<T>.defaultTimeout() = timeout(20, TimeUnit.SECONDS)

interface SubscriptionTracker {
    /** Clear Subscriptions. */
    fun cancelSubscriptions()

    /** Start tracking a disposable. */
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