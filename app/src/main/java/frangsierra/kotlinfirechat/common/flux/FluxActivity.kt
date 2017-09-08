package frangsierra.kotlinfirechat.common.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import frangsierra.kotlinfirechat.common.dagger.ComponentFactory
import frangsierra.kotlinfirechat.common.dagger.ComponentManager
import frangsierra.kotlinfirechat.common.dagger.inject
import frangsierra.kotlinfirechat.common.rx.DefaultSubscriptionTracker
import frangsierra.kotlinfirechat.common.rx.SubscriptionTracker
import javax.inject.Inject


abstract class FluxActivity<T : Any> :
        AppCompatActivity(),
        SubscriptionTracker by DefaultSubscriptionTracker(),
        ComponentManager by app {

    @Inject lateinit var dispatcher: Dispatcher
    private val componentFactory: ComponentFactory<T> by lazy {
        onCreateComponentFactory()
    }

    abstract fun onCreateComponentFactory(): ComponentFactory<T>

    val component: T by lazy {
        findComponent(componentFactory.componentType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            app.registerComponent(componentFactory)
        }
        inject(component, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            //This won't be called if app is killed!
            app.unregisterComponent(componentFactory)
        }
        cancelSubscriptions()
    }
}