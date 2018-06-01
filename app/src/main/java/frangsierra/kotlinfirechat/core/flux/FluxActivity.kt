package frangsierra.kotlinfirechat.core.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import frangsierra.kotlinfirechat.core.dagger.inject
import frangsierra.kotlinfirechat.core.flux.appComponent
import mini.DefaultSubscriptionTracker
import mini.Dispatcher
import mini.SubscriptionTracker
import javax.inject.Inject

abstract class FluxActivity : AppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject
    lateinit protected var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appComponent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}