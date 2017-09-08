package frangsierra.kotlinfirechat.common.flux

import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import frangsierra.kotlinfirechat.common.rx.DefaultSubscriptionTracker
import frangsierra.kotlinfirechat.common.rx.SubscriptionTracker
import javax.inject.Inject

/**
 * Custom [Fragment] capable of tracking subscriptions and cancel them when the fragment
 * is destroyed in order to avoid possible memory leaks.
 */
open class FluxFragment :
    Fragment(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject lateinit var dispatcher: Dispatcher

    @Suppress("KDocMissingDocumentation")
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        cancelSubscriptions()
    }
}