package frangsierra.kotlinfirechat

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import frangsierra.kotlinfirechat.util.onError
import gg.grizzlygrit.common.log.Grove
import kotlinx.android.synthetic.main.login_activity.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    val authenticationListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { auth -> loggedUser = auth.currentUser }
    val indeterminateProgressDialog by lazy { indeterminateProgressDialog("Login in") }

    var loggedUser: FirebaseUser? by Delegates.observable(null as FirebaseUser?) { _, old, new ->
        //Every time the logged user value change we check it to move to the new activity
        if (old == null && new != null)
            startActivity(Intent(this, ChatActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        loginPasswordButton.setOnClickListener {
            if (!fieldsAreFilled()) return@setOnClickListener
            indeterminateProgressDialog.show()
            doAsync {
                auth.signInWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
                        .addOnCompleteListener { authResult ->
                            indeterminateProgressDialog.hide()
                            authResult.exception?.let {
                                toast(it.message.toString())
                                return@addOnCompleteListener
                            }
                            Grove.i { "New user logged : ${authResult.result.user}" }
                        }.addOnFailureListener { error -> toast(error.message.toString()) }
            }
        }
        loginGoogleButton.setOnClickListener { }

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

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authenticationListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authenticationListener)
    }
}
