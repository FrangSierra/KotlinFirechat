package frangsierra.kotlinfirechat.session

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.common.flux.FluxActivity
import frangsierra.kotlinfirechat.session.store.SessionStore
import frangsierra.kotlinfirechat.util.GoogleLoginCallback
import frangsierra.kotlinfirechat.util.dismissProgressDialog
import frangsierra.kotlinfirechat.util.onError
import frangsierra.kotlinfirechat.util.toast
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject

class LoginActivity : FluxActivity(), GoogleLoginCallback {

    @Inject
    lateinit var sessionStore: SessionStore

    override val googleApiClient: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
    }

    override val googleSingInClient: GoogleSignInClient by lazy { GoogleSignIn.getClient(this, googleApiClient) }


    override fun onGoogleCredentialReceived(credential: AuthCredential, account: GoogleSignInAccount) {
        loginWithCredential(credential = credential, email = account.email!!)
    }

    override fun onGoogleSignInFailed(e: ApiException) {
        dismissProgressDialog()
        toast(e.toString())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        initializeInterface()
        startListeningStoreChanges()
    }

    private fun startListeningStoreChanges() {

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //How to retrieve SHA1 for Firebase Google Sign In https://stackoverflow.com/questions/15727912/sha-1-fingerprint-of-keystore-certificate
    private fun loginWithCredential(credential: AuthCredential, email: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        manageGoogleResult(requestCode, data)
    }
}
