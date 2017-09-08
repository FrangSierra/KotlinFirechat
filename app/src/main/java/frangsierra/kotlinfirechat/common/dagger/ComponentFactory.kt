package frangsierra.kotlinfirechat.common.dagger

import android.content.ComponentCallbacks2
import kotlin.reflect.KClass

typealias ComponentKey = String

/**
 * Common interface to allow component tracking,
 * used to share Dagger components between multiple activities.
 */
interface ComponentFactory<T : Any> {

    /**
     * Factory method to create a component.
     */
    fun createComponent(): T

    fun destroyComponent(component: T) {

    }

    /**
     * When this component should be destroyed, see [DestroyStrategy].
     */
    val destroyStrategy: DestroyStrategy
        get() = DestroyStrategy.REF_COUNT

    /**
     * Components that should be kept active while this component is active.
     * All dependencies must exist upon registration time.
     */
    val dependencies: List<KClass<*>>
        get() = emptyList()

    /**
     * The type of the component to add. If no such component exists with the same
     * name [createComponent] will be called.
     */
    val componentType: KClass<T>
}


enum class DestroyStrategy(val trimMemoryValue: Int) {
    /**
     * Component is destroyed when there are no more holders depending on it.
     */
    REF_COUNT(Int.MAX_VALUE),
    TRIM_MEMORY_COMPLETE(ComponentCallbacks2.TRIM_MEMORY_COMPLETE),
    TRIM_MEMORY_MODERATE(ComponentCallbacks2.TRIM_MEMORY_MODERATE),
    TRIM_MEMORY_BACKGROUND(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND),
    TRIM_MEMORY_RUNNING_CRITICAL(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL),
    TRIM_MEMORY_RUNNING_LOW(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW),
    TRIM_MEMORY_RUNNING_MODERATE(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE),
}