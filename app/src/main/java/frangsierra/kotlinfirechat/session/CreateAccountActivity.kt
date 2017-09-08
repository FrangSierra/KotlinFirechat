package frangsierra.kotlinfirechat.session

import frangsierra.kotlinfirechat.chat.ChatActivity
import frangsierra.kotlinfirechat.common.dagger.AppComponent
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.util.onError
import frangsierra.kotlinfirechat.util.tryToGetLoginMessage
import kotlinx.android.synthetic.main.create_account_activity.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class CreateAccountActivity : frangsierra.kotlinfirechat.common.flux.FluxActivity<AppComponent>() {
    override fun onCreateComponentFactory() = frangsierra.kotlinfirechat.common.dagger.AppComponentFactory

    @javax.inject.Inject lateinit var sessionStore: frangsierra.kotlinfirechat.session.SessionStore

    val indeterminateProgressDialog by lazy { indeterminateProgressDialog("Creating account") }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(frangsierra.kotlinfirechat.R.layout.create_account_activity)

        initializeInterface()
        startListeningStoreChanges()
    }

    private fun initializeInterface() {

        createAccountButton.setOnClickListener {
            if (!fieldsAreFilled()) return@setOnClickListener
            val user = User(userName = editTextUsername.text.toString(), email = editTextEmail.text.toString())
            dispatcher.dispatch(frangsierra.kotlinfirechat.session.CreateAccountWithEmailAction(user, editTextPassword.text.toString()))

        }
        signInButton.setOnClickListener {
            startActivity(android.content.Intent(this, LoginActivity::class.java)
                .apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK })
        }
    }

    private fun startListeningStoreChanges() {
        sessionStore.flowable()
            .map { it.status }
            .distinctUntilChanged()
            .subscribe {
                when (it) {
                    frangsierra.kotlinfirechat.session.LoginStatus.CREATING_ACCOUNT -> indeterminateProgressDialog.show()
                    frangsierra.kotlinfirechat.session.LoginStatus.LOGGED -> {
                        indeterminateProgressDialog.dismiss()
                        startActivity(android.content.Intent(this, ChatActivity::class.java)
                            .apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK })
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
}
