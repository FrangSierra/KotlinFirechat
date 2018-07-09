package frangsierra.kotlinfirechat.core

import android.content.Intent
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import frangsierra.kotlinfirechat.core.flux.FluxActivity
import frangsierra.kotlinfirechat.home.HomeActivity
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.session.store.SessionStore
import frangsierra.kotlinfirechat.session.store.TryToLoginInFirstInstanceAction
import frangsierra.kotlinfirechat.util.filterOne
import frangsierra.kotlinfirechat.util.toast
import javax.inject.Inject

class SplashActivity : FluxActivity() {

    @Inject
    lateinit var sessionStore: SessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkPlayServices()) {
            dispatcher.dispatch(TryToLoginInFirstInstanceAction())
            sessionStore.flowable()
                    .filterOne { it.loginTask.isTerminal() }
                    .subscribe { status ->
                        if (status.loginTask.isSuccessful()) goToHome()
                        else goToOnLogin()
                    }.track()
        }
    }

    private fun goToHome() {
        val intent = HomeActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun goToOnLogin() {
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    /**
     * Check the device to make sure it has the proper Google Play Services version.
     * If it doesn't, display a dialog that allows users to download it from
     * Google Play or enable it in the device's system settings.
     */
    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 666).show()
            } else {
                toast("Google play is not supported in this device")
                finish()
            }
            return false
        }
        return true
    }
}
