package frangsierra.kotlinfirechat.session

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.GoogleAuthProvider
import frangsierra.kotlinfirechat.chat.ChatActivity
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.common.dagger.AppComponent
import frangsierra.kotlinfirechat.common.dagger.AppComponentFactory
import frangsierra.kotlinfirechat.common.flux.FluxActivity
import frangsierra.kotlinfirechat.util.onError
import frangsierra.kotlinfirechat.util.tryToGetLoginMessage
import kotlinx.android.synthetic.main.login_activity.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import javax.inject.Inject

private const val RC_SIGN_IN = 123

class LoginActivity : FluxActivity<AppComponent>(), GoogleApiClient.OnConnectionFailedListener {
    override fun onCreateComponentFactory() = AppComponentFactory

    @Inject lateinit var sessionStore: SessionStore
    @Inject lateinit var googleApiClient: GoogleApiClient

    val indeterminateProgressDialog by lazy { indeterminateProgressDialog("Creating account") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_activity)

        initializeInterface()
        startListeningStoreChanges()
    }

    private fun startListeningStoreChanges() {
        sessionStore.flowable()
            .map { it.status }
            .distinctUntilChanged()
            .subscribe {
                when (it) {
                    LoginStatus.LOGGED -> {
                        indeterminateProgressDialog.dismiss()
                        startActivity(Intent(this, ChatActivity::class.java)
                            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                    }
                }
            }.track()

        sessionStore.flowable()
            .filter { it.throwable != null }
            .map<Throwable> { it.throwable!! }
            .distinctUntilChanged()
            .subscribe {
                indeterminateProgressDialog.dismiss()
                toast(getString(it!!.tryToGetLoginMessage()))
            }.track()
    }

    private fun initializeInterface() {
        loginPasswordButton.setOnClickListener {
            if (!fieldsAreFilled()) return@setOnClickListener
            indeterminateProgressDialog.show()
            dispatcher.dispatch(LoginWithEmailAndPasswordAction(editTextEmail.text.toString(), editTextPassword.text.toString()))
        }

        loginGoogleButton.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        createAccountText.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
    }

    private fun fieldsAreFilled(): Boolean {
        editTextEmail.text.toString().takeIf { it.isEmpty() }?.let {
            inputEmail.onError(getString(R.string.error_cannot_be_empty))
            return false
        }
        inputEmail.onError(null, false)
        editTextPassword.text.toString().takeIf { it.isEmpty() }?.let {
            inputPassword.onError(getString(R.string.error_cannot_be_empty))
            return false
        }
        inputPassword.onError(null, false)
        return true
    }

    //How to retrieve SHA1 for Firebase Google Sign In https://stackoverflow.com/questions/15727912/sha-1-fingerprint-of-keystore-certificate
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Google Sign In was successful, authenticate with Firebase
                val account = result.signInAccount
                val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
                dispatcher.dispatch(ManageCredentialAction(credential, User(userName = account.displayName, email = account.email)))
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
