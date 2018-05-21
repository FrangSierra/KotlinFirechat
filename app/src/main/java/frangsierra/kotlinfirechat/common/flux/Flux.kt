package frangsierra.kotlinfirechat.common.flux

import frangsierra.kotlinfirechat.BuildConfig
import frangsierra.kotlinfirechat.common.log.Grove
import frangsierra.kotlinfirechat.common.misc.assertNotOnUiThread
import frangsierra.kotlinfirechat.common.misc.assertOnUiThread
import frangsierra.kotlinfirechat.common.misc.onUi
import frangsierra.kotlinfirechat.common.misc.onUiSync
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure


private val actionTagsCache = HashMap<Class<*>, Set<Class<*>>>()

/**
 * Common interface for all actions.
 * Tags must be types that this action implements.
 * Defaults to Any and the runtime type.
 */
interface Action {

    /**
     * List of types this action may be observed by.
     */
    val tags: Set<Class<*>>
        get() {
            return actionTagsCache.getOrPut(this::class.java) {
                return reflectActionTypes(this::class)
            }
        }
}

internal fun reflectActionTypes(type: KClass<*>): Set<Class<*>> {
    return type.supertypes
            .map { (it.jvmErasure.java as Class<*>).kotlin }
            .map { reflectActionTypes(it) }
            .flatten()
            .plus(type.java)
            .toSet()
}

/**
 * Debug Action that captures trace information when its created.
 * It has no effect on release builds.
 */
abstract class TracedAction : Action {
    val trace: Array<StackTraceElement>? = let {
        if (BuildConfig.DEBUG) {
            Throwable().stackTrace
        } else {
            null
        }
    }
}


typealias Interceptor = (action: Action, chain: Chain) -> Action

val actionCounter = AtomicInteger()

/**
 * A chain of interceptors. Call [.proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain {
    fun proceed(action: Action): Action
}


val HIGH_PRIORITY: Int = 0
val MEDIUM_PRIORITY: Int = 50

/**
 * Dispatch actions and subscribe to them in order to produce changes.
 */
class Dispatcher(var verifyThreads: Boolean = true) {
    val DEFAULT_PRIORITY: Int = 100

    val subscriptionCount: Int get() = subscriptionMap.values.map { it?.size ?: 0 }.sum()
    var dispatching: Boolean = false
        private set

    private val subscriptionMap = HashMap<Class<*>, TreeSet<DispatcherSubscription<Any>>?>()
    private var subscriptionCounter = AtomicInteger()

    private val interceptors = ArrayList<Interceptor>()
    private val rootChain: Chain = object : Chain {
        override fun proceed(action: Action): Action {
            action.tags.forEach { tag ->
                subscriptionMap[tag]?.let { set ->
                    set.forEach { it.onAction(action) }
                }
            }
            return action
        }
    }
    private var chain = rootChain
    private fun buildChain(): Chain {
        return interceptors.fold(rootChain)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action = interceptor(action, chain)
            }
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            chain = buildChain()
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            chain = buildChain()
        }
    }

    fun dispatch(action: Action) {
        if (verifyThreads) assertOnUiThread()
        synchronized(this) {
            try {
                if (dispatching) error("Can't dispatch actions while reducing state!")
                actionCounter.incrementAndGet()
                dispatching = true
                chain.proceed(action)
            } finally {
                dispatching = false
            }
        }
    }

    /**
     * Post an event that will dispatch the action on the Ui thread
     * and return immediately.
     */
    fun dispatchOnUi(action: Action) {
        onUi { dispatch(action) }
    }

    /**
     * Post and event that will dispatch the action on the Ui thread
     * and block until the dispatch is complete.
     *
     * Can't be called from the main thread.
     */
    fun dispatchOnUiSync(action: Action) {
        if (verifyThreads) assertNotOnUiThread()
        onUiSync { dispatch(action) }
    }

    fun <T : Any> subscribe(tag: KClass<T>, fn: (T) -> Unit = {}) = subscribe(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> subscribe(priority: Int,
                            tag: KClass<T>,
                            fn: (T) -> Unit = {}): DispatcherSubscription<T> {
        val subscription = DispatcherSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                fn)
        return registerInternal(subscription)
    }

    internal fun <T : Any> registerInternal(dispatcherSubscription: DispatcherSubscription<T>): DispatcherSubscription<T> {
        @Suppress("UNCHECKED_CAST")
        synchronized(this) {
            subscriptionMap.getOrPut(dispatcherSubscription.tag, {
                TreeSet({ a, b ->
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                })
            })!!.add(dispatcherSubscription as DispatcherSubscription<Any>)
        }
        return dispatcherSubscription
    }

    internal fun <T : Any> unregisterInternal(dispatcherSubscription: DispatcherSubscription<T>) {
        synchronized(this) {
            val set = subscriptionMap[dispatcherSubscription.tag] as? TreeSet<*>
            val removed = set?.remove(dispatcherSubscription) == true
            if (!removed) {
                Grove.w { "Failed to remove dispatcherSubscription, multiple dispose calls?" }
            }
        }
    }
}

class DispatcherSubscription<T : Any>(internal val dispatcher: Dispatcher,
                                      internal val id: Int,
                                      internal val priority: Int,
                                      internal val tag: Class<T>,
                                      private val cb: (T) -> Unit) : Disposable {
    private var processor: PublishProcessor<T>? = null
    private var subject: PublishSubject<T>? = null
    private var disposed = false

    override fun isDisposed(): Boolean = disposed

    internal fun onAction(action: T) {
        if (disposed) {
            Grove.e { "Subscription is disposed but got an action: $action" }
            return
        }
        cb.invoke(action)
        processor?.onNext(action)
        subject?.onNext(action)
    }

    fun flowable(): Flowable<T> {
        if (processor == null) {
            synchronized(this) {
                if (processor == null) processor = PublishProcessor.create()
            }
        }
        return processor!!
    }

    fun observable(): Observable<T> {
        if (subject == null) {
            synchronized(this) {
                if (subject == null) subject = PublishSubject.create()
            }
        }
        return subject!!
    }

    override fun dispose() {
        if (disposed) return
        synchronized(this) {
            dispatcher.unregisterInternal(this)
            disposed = true
            processor?.onComplete()
            subject?.onComplete()
        }
    }
}
