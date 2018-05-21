package frangsierra.kotlinfirechat.common.flux

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import frangsierra.kotlinfirechat.common.log.Grove
import mini.flux.OnTrimMemoryAction

/**
 * Handy alias to use with dagger
 */
typealias StoreMap = Map<Class<*>, Store<*>>

typealias LazyStoreMap = dagger.Lazy<Map<Class<*>, Store<*>>>


/**
 * Sort and create Stores initial state.
 */
fun initStores(uninitializedStores: List<Store<*>>) {
    val now = System.currentTimeMillis()

    val stores = uninitializedStores.sortedBy { it.properties.initOrder }

    val initTimes = LongArray(stores.size)
    for (i in 0..stores.size - 1) {
        val start = System.currentTimeMillis()
        stores[i].init()
        stores[i].state //Create initial state
        initTimes[i] += System.currentTimeMillis() - start
    }

    val elapsed = System.currentTimeMillis() - now

    Grove.d { "┌ Application with ${stores.size} stores loaded in $elapsed ms" }
    Grove.d { "├────────────────────────────────────────────" }
    for (i in 0..stores.size - 1) {
        val store = stores[i]
        var boxChar = "├"
        if (store === stores[stores.size - 1]) {
            boxChar = "└"
        }
        Grove.d { "$boxChar ${store.javaClass.simpleName} - ${initTimes[i]} ms" }
    }
}

/**
 * Register callbacks to send [OnTrimMemoryAction] and [OnActivityLifeCycleAction].
 */
fun registerSystemCallbacks(dispatcher: Dispatcher, context: Context) {
    val app = context.applicationContext as? Application

    app?.registerComponentCallbacks(object : ComponentCallbacks2 {
        override fun onLowMemory() {}

        override fun onConfigurationChanged(newConfig: Configuration?) {}

        override fun onTrimMemory(level: Int) {
            dispatcher.dispatch(OnTrimMemoryAction(level))
        }
    })

    //Uncomment to debug
//    app?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
//        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?)
//                = dispatcher.dispatch(OnActivityLifeCycleAction(activity, OnActivityLifeCycleAction.ActivityStage.CREATED))
//
//        override fun onActivityStarted(activity: Activity)
//                = dispatcher.dispatch(OnActivityLifeCycleAction(activity, OnActivityLifeCycleAction.ActivityStage.STARTED))
//
//        override fun onActivityResumed(activity: Activity)
//                = dispatcher.dispatch(OnActivityLifeCycleAction(activity, OnActivityLifeCycleAction.ActivityStage.RESUMED))
//
//        override fun onActivityPaused(activity: Activity)
//                = dispatcher.dispatch(OnActivityLifeCycleAction(activity, OnActivityLifeCycleAction.ActivityStage.PAUSED))
//
//        override fun onActivityStopped(activity: Activity)
//                = dispatcher.dispatch(OnActivityLifeCycleAction(activity, OnActivityLifeCycleAction.ActivityStage.STOPPED))
//
//        override fun onActivityDestroyed(activity: Activity)
//                = dispatcher.dispatch(OnActivityLifeCycleAction(activity, OnActivityLifeCycleAction.ActivityStage.DESTROYED))
//
//        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
//    })
}

