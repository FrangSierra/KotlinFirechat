package frangsierra.kotlinfirechat.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import frangsierra.kotlinfirechat.home.HomeActivity
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.core.flux.FluxActivity
import frangsierra.kotlinfirechat.session.store.LoginWithCredentials
import frangsierra.kotlinfirechat.session.store.LoginWithProviderCredentials
import frangsierra.kotlinfirechat.session.store.SessionStore
import frangsierra.kotlinfirechat.util.*
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject

class LoginActivity : FluxActivity(), GoogleLoginCallback {

    @Inject
    lateinit var sessionStore: SessionStore

    companion object {
        fun newIntent(context: Context): Intent =
                Intent(context, LoginActivity::class.java)
    }

    override val googleApiClient: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    override val googleSingInClient: GoogleSignInClient by lazy { GoogleSignIn.getClient(this, googleApiClient) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        initializeInterface()
    }

    private fun initializeInterface() {
        loginPasswordButton.setOnClickListener { loginWithEmailAndPassword() }
        loginGoogleButton.setOnClickListener { logInWithGoogle(this) }
        createAccountText.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
    }

    private fun loginWithEmailAndPassword() {
        if (!fieldsAreFilled()) return
        showProgressDialog("Logging")
        dispatcher.dispatch(LoginWithCredentials(editTextEmail.text.toString(), editTextPassword.text.toString()))
        sessionStore.flowable()
            .filterOne { it.loginTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.loginTask.isSuccessful() && it.loggedUser != null) {
                    if (it.verified) goToHome() else goToVerificationEmailScreen()
                } else if (it.loginTask.isFailure()) {
                    it.loginTask.error!!.message?.let { toast(it) }
                }
                dismissProgressDialog()
            }.track()
    }

    //How to retrieve SHA1 for Firebase Google Sign In https://stackoverflow.com/questions/15727912/sha-1-fingerprint-of-keystore-certificate
    override fun onGoogleCredentialReceived(credential: AuthCredential, account: GoogleSignInAccount) {
        showProgressDialog("Logging")
        dispatcher.dispatch(LoginWithProviderCredentials(credential, account.email!!))
        sessionStore.flowable()
            .filterOne { it.loginTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.loginTask.isSuccessful() && it.loggedUser != null) {
                    if (it.verified) goToHome() else goToVerificationEmailScreen()
                } else if (it.loginTask.isFailure()) {
                    it.loginTask.error!!.message?.let { toast(it) }
                }
                dismissProgressDialog()
            }.track()
    }

    override fun onGoogleSignInFailed(e: ApiException) {
        dismissProgressDialog()
        toast(e.toString())
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

    private fun goToHome() {
        dismissProgressDialog()
        val intent = HomeActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun goToVerificationEmailScreen() {
        dismissProgressDialog()
        EmailVerificationActivity.startActivity(this, sessionStore.state.loggedUser!!.email)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        manageGoogleResult(requestCode, data)
    }
}
