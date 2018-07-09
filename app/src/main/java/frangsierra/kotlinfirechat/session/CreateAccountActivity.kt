package frangsierra.kotlinfirechat.session

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import frangsierra.kotlinfirechat.home.HomeActivity
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.core.errors.ErrorHandler
import frangsierra.kotlinfirechat.core.flux.FluxActivity
import frangsierra.kotlinfirechat.session.store.CreateAccountWithCredentialsAction
import frangsierra.kotlinfirechat.session.store.CreateAccountWithProviderCredentialsAction
import frangsierra.kotlinfirechat.session.store.SessionStore
import frangsierra.kotlinfirechat.util.*
import kotlinx.android.synthetic.main.create_account_activity.*
import javax.inject.Inject

class CreateAccountActivity : FluxActivity(), GoogleLoginCallback {

    @Inject
    lateinit var sessionStore: SessionStore
    @Inject
    lateinit var errorHandler: ErrorHandler

    override val googleApiClient: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    override val googleSingInClient: GoogleSignInClient by lazy { GoogleSignIn.getClient(this, googleApiClient) }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(frangsierra.kotlinfirechat.R.layout.create_account_activity)

        initializeInterface()
    }

    private fun initializeInterface() {
        createAccountButton.setOnClickListener { signInWithEmailAndPassword() }
        createGoogleButton.setOnClickListener { logInWithGoogle(this) }
    }

    private fun signInWithEmailAndPassword() {
        if (!fieldsAreFilled()) return
        showProgressDialog("Creating account")
        dispatcher.dispatch(CreateAccountWithCredentialsAction(editTextEmail.text.toString(), editTextPassword.text.toString(), editTextUsername.text.toString()))
        sessionStore.flowable()
            .filterOne { it.createAccountTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.createAccountTask.isSuccessful() && it.loggedUser != null) {
                    if (it.verified) goToHome() else goToVerificationEmailScreen()
                } else if (it.createAccountTask.isFailure()) {
                    it.createAccountTask.error?.let {
                        toast(errorHandler.getMessageForError(it))
                    }
                }
                dismissProgressDialog()
            }.track()
    }

    //How to retrieve SHA1 for Firebase Google Sign In https://stackoverflow.com/questions/15727912/sha-1-fingerprint-of-keystore-certificate
    override fun onGoogleCredentialReceived(credential: AuthCredential, account: GoogleSignInAccount) {
        showProgressDialog("Creating account")
        dispatcher.dispatch(CreateAccountWithProviderCredentialsAction(credential, GoogleSignInApiUtils.getUserData(account)))
        sessionStore.flowable()
            .filterOne { it.createAccountTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.createAccountTask.isSuccessful() && it.loggedUser != null) {
                    if (it.verified) goToHome() else goToVerificationEmailScreen()
                } else if (it.createAccountTask.isFailure()) {
                    it.createAccountTask.error?.let {
                        toast(errorHandler.getMessageForError(it))
                    }
                }
                dismissProgressDialog()
            }.track()
    }

    override fun onGoogleSignInFailed(e: ApiException) {
        dismissProgressDialog()
        toast(e.toString())
    }

    private fun fieldsAreFilled(): Boolean {
        editTextUsername.text.toString().takeIf { it.isEmpty() }?.let {
            inputUsername.onError(getString(frangsierra.kotlinfirechat.R.string.error_cannot_be_empty))
            return false
        }
        inputUsername.onError(null, false)

        editTextEmail.text.toString().takeIf { it.isEmpty() }?.let {
            inputEmail.onError(getString(frangsierra.kotlinfirechat.R.string.error_cannot_be_empty))
            return false
        }
        inputEmail.onError(null, false)

        editTextPassword.text.toString().takeIf { it.isEmpty() }?.let {
            inputPassword.onError(getString(frangsierra.kotlinfirechat.R.string.error_cannot_be_empty))
            return false
        }
        editTextPassword.text.toString().takeIf { it.length < 6 }?.let {
            inputPassword.onError(getString(frangsierra.kotlinfirechat.R.string.error_invalid_password_not_valid))
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
