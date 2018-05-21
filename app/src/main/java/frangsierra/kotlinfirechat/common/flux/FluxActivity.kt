package frangsierra.kotlinfirechat.common.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import frangsierra.kotlinfirechat.common.dagger.inject
import frangsierra.kotlinfirechat.common.rx.DefaultSubscriptionTracker
import frangsierra.kotlinfirechat.common.rx.SubscriptionTracker
import javax.inject.Inject

abstract class FluxActivity : AppCompatActivity(),
        SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject lateinit protected var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appComponent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}