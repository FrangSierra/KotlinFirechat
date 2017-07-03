package frangsierra.kotlinfirechat

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import frangsierra.kotlinfirechat.util.onError
import gg.grizzlygrit.common.log.Grove
import kotlinx.android.synthetic.main.create_account_activity.*
import org.jetbrains.anko.custom.onUiThread
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast


class CreateAccountActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account_activity)

        createAccountButton.setOnClickListener {
            if (!fieldsAreFilled()) return@setOnClickListener
            doAsync {
                auth.createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.toString())
                        .addOnCompleteListener { authResult ->
                            authResult.exception?.let {
                                toast(it.message.toString())
                                return@addOnCompleteListener
                            }
                            updateProfileAfterAccountCreation(authResult.result.user)
                        }.addOnFailureListener { error -> toast(error.message.toString()) }
            }
        }

        signInButton.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }) }
    }

    private fun fieldsAreFilled(): Boolean {
        editTextUsername.text.toString().takeIf { it.isEmpty() }?.let {
            inputUsername.onError(getString(R.string.error_cannot_be_empty))
            return false
        }
        inputUsername.onError(null, false)

        editTextEmail.text.toString().takeIf { it.isEmpty() }?.let {
            inputEmail.onError(getString(R.string.error_cannot_be_empty))
            return false
        }
        inputEmail.onError(null, false)

        editTextPassword.text.toString().takeIf { it.isEmpty() }?.let {
            inputPassword.onError(getString(R.string.error_cannot_be_empty))
            return false
        }
        editTextPassword.text.toString().takeIf { it.length < 6 }?.let {
            inputPassword.onError(getString(R.string.error_invalid_password_not_valid))
            return false
        }
        inputPassword.onError(null, false)
        return true
    }

    private fun updateProfileAfterAccountCreation(createdUser: FirebaseUser) {
        val newProfileRequest = UserProfileChangeRequest.Builder().setDisplayName(editTextUsername.text.toString())
        doAsync {
            createdUser.updateProfile(newProfileRequest.build())
                    .addOnCompleteListener {
                        Grove.i{"New user $createdUser correctly updated with new username"}
                        onUiThread { startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }) }
                    }
        }
    }
}
