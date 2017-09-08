package frangsierra.kotlinfirechat.common.flux

import frangsierra.kotlinfirechat.common.rx.DefaultSubscriptionTracker
import frangsierra.kotlinfirechat.common.rx.SubscriptionTracker
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

abstract class Store<S : Any> : SubscriptionTracker by DefaultSubscriptionTracker() {

    open val properties: StoreProperties = StoreProperties()

    private var initialized = false
    private var _state: S? = null
    val processor = PublishProcessor.create<S>()

    var state: S
        get() {
            if (_state == null) {
                synchronized(this) {
                    if (_state == null) _state = initialState()
                }
            }
            return _state!!
        }
        set(value) {
            if (value != _state) {
                _state = value
                processor.onNext(value)
            }
        }

    @Suppress("UNCHECKED_CAST")
    protected open fun initialState(): S {
        try {
            val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                    as Class<S>
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state", e)
        }
    }

    fun flowable(): Flowable<S> {
        return processor.startWith { s ->
            s.onNext(state)
            s.onComplete()
        }
    }

    /**
     * Perform initialization, will have effect exactly once.
     */
    fun initOnce() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            initialized = true
            init()
        }
    }

    /**
     * Initialize the store. Called after all stores instances are ready.
     */
    protected abstract fun init()

    /**
     * Release resources.
     */
    fun dispose() {
        //No-op
    }
}

/**
 * Type safe store lookup.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Store<*>> StoreMap.find(clazz: KClass<T>): T {
    val java: Class<Store<*>> = (clazz.java) as Class<Store<*>>
    return this[java]!! as T
}

//fun  Store<*>.combined(@Nonnull store: Store<*>): Flowable<Store<*>> = combined(listOf(store))
//
//fun  Store<*>.combined(@Nonnull stores: List<Store<*>>): Flowable<Store<*>> {
//    return flowable().map { this }
//            .apply {
//                stores.forEach { store ->
//                    mergeWith(store.flowable().map { store })
//                }
//            }
//}

/**
 * Store meta properties.
 *
 * @param initOrder After construction invocation priority. Higher is lower.
 * @param logFn Optional function used to represent state in a human readable form.
 *              If null, current state string function will be used.
 */
data class StoreProperties(
        val initOrder: Int = DEFAULT_INIT_PRIORITY,
        val logFn: (() -> String)? = null) {
    companion object {
        val DEFAULT_INIT_PRIORITY = 100
    }
}