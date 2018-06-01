package frangsierra.kotlinfirechat.session.controller

import com.google.firebase.auth.*
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.core.firebase.User
import frangsierra.kotlinfirechat.core.firebase.associatedProviders
import frangsierra.kotlinfirechat.core.firebase.toUser
import frangsierra.kotlinfirechat.session.model.FirebaseUserNotFound
import frangsierra.kotlinfirechat.session.model.ProviderNotLinkedException
import frangsierra.kotlinfirechat.session.store.CreateAccountCompleteAction
import frangsierra.kotlinfirechat.session.store.LoginCompleteAction
import frangsierra.kotlinfirechat.session.store.VerificationEmailSentAction
import frangsierra.kotlinfirechat.session.store.VerifyUserEmailCompleteAction
import frangsierra.kotlinfirechat.util.taskFailure
import frangsierra.kotlinfirechat.util.taskSuccess
import mini.Dispatcher
import javax.inject.Inject

interface SessionController {

    /**
     * To control the splash, when the app is launched try to make a first login.
     */
    fun tryToLoginInFirstInstance()

    /**
     * Try to signIn in the Firebase Auth API with a email and a password, returning an [AuthResult]
     * if it's successful or a [FirebaseAuthException] if fails.
     *
     * @param email email of the user
     * @param password password introduced
     */
    fun loginWithCredentials(email: String, password: String)

    /**
     * Call to the server to create a new user account using a email and a password.
     *
     * @param email email of the user
     * @param password password introduced
     * @param username username for the user
     */
    fun createAccountWithCredentials(email: String, password: String, username: String)

    /**
     * Try to signIn in the Firebase Auth API with a credential, returning an [AuthResult]
     * if it's successful, a [ProviderNotLinkedException] if the user is trying to login without register first
     * the credential, or an [FirebaseAuthException] if fails.
     *
     * @param credential the [AuthCredential] with all the info of the provider
     * @param email email of the user
     */
    fun loginWithProviderCredentials(credential: AuthCredential, email: String)

    /**
     * Call to the server to create a new user account using a [AuthCredential] from a provider.
     *
     * @param credential the [AuthCredential] with all the info of the provider
     * @param userData data of the user
     */
    fun createAccountWithProviderCredentials(credential: AuthCredential, userData: User)

    /**
     * Send a verification email to the user logged in the auth instance.
     */
    fun sendVerificationEmail()

    /**
     * Sign out the firebase auth instance.
     */
    fun signOut()

    /**
     * Refresh the instance of the [FirebaseUser] with all the updated data from the server to verify the email.
     */
    fun verifyUser()
}

@AppScope
class SessionControllerImpl @Inject constructor(private val authInstance: FirebaseAuth,
                                                private val dispatcher: Dispatcher) : SessionController {
    override fun tryToLoginInFirstInstance() {
        authInstance.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null || !firebaseAuth.currentUser!!.isEmailVerified)
                dispatcher.dispatchOnUi(LoginCompleteAction(task = taskFailure()))
            else {
                val currentUser = firebaseAuth.currentUser!!
                dispatcher.dispatchOnUiSync(LoginCompleteAction(task = taskSuccess(),
                    user = currentUser.toUser(),
                    associatedProviders = currentUser.associatedProviders()))
            }
        }
    }

    override fun loginWithCredentials(email: String, password: String) {
        authInstance.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val emailVerified = result.user.isEmailVerified
                    val user = result.user.toUser()
                    val providers = result.user.associatedProviders()
                    dispatcher.dispatchOnUi(LoginCompleteAction(
                        user = user,
                        emailVerified = emailVerified,
                        task = taskSuccess(),
                        associatedProviders = providers))
                } else {
                    dispatcher.dispatchOnUi(LoginCompleteAction(user = null, task = taskFailure(task.exception)))
                }
            }
    }

    override fun createAccountWithCredentials(email: String, password: String, username: String) {
        authInstance.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = task.result.user
                val emailVerified = firebaseUser.isEmailVerified
                val user = firebaseUser.toUser()
                val providers = firebaseUser.associatedProviders()
                dispatcher.dispatchOnUi(CreateAccountCompleteAction(
                    user = user,
                    emailVerified = emailVerified,
                    task = taskSuccess(),
                    associatedProviders = providers))
                if (!emailVerified) sendVerificationEmailToUser(firebaseUser)
            } else {
                dispatcher.dispatchOnUi(CreateAccountCompleteAction(
                    user = null,
                    task = taskFailure(task.exception)))
            }
        }
    }

    override fun loginWithProviderCredentials(credential: AuthCredential, email: String) {
        authInstance.fetchSignInMethodsForEmail(email).addOnCompleteListener { fetchTask ->
            if (fetchTask.isSuccessful) {
                val providerResult = fetchTask.result
                val isNewAccount = providerResult.signInMethods!!.contains((credential.provider))
                if (isNewAccount) {
                    authInstance.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            val authResult = signInTask.result
                            val emailVerified = authResult.user.isEmailVerified
                            val user = authResult.user.toUser()
                            val providers = authResult.user.associatedProviders()
                            dispatcher.dispatchOnUi(LoginCompleteAction(
                                user = user,
                                emailVerified = emailVerified,
                                task = taskSuccess(),
                                associatedProviders = providers))
                            if (!emailVerified) sendVerificationEmailToUser(authResult.user)
                        } else dispatcher.dispatchOnUi(LoginCompleteAction(user = null, task = taskFailure(fetchTask.exception)))
                    }
                } else {
                    dispatcher.dispatchOnUi(LoginCompleteAction(user = null, task = taskFailure(ProviderNotLinkedException(credential.provider))))
                }
            } else {
                dispatcher.dispatchOnUi(LoginCompleteAction(user = null, task = taskFailure(fetchTask.exception)))
            }
        }
    }

    override fun createAccountWithProviderCredentials(credential: AuthCredential, userData: User) {
        authInstance.fetchSignInMethodsForEmail(userData.email).addOnCompleteListener { fetchTask ->
            if (fetchTask.isSuccessful) {
                val providerResult = fetchTask.result
                val isNewAccount = providerResult.signInMethods!!.contains((credential.provider))
                authInstance.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        val authResult = signInTask.result
                        val emailVerified = authResult.user.isEmailVerified
                        val user = authResult.user.toUser().copy(photoUrl = userData.photoUrl)
                        val providers = authResult.user.associatedProviders()
                        dispatcher.dispatchOnUi(CreateAccountCompleteAction(
                            user = user,
                            alreadyExisted = !isNewAccount,
                            emailVerified = emailVerified,
                            task = taskSuccess(),
                            associatedProviders = providers))
                        if (!emailVerified) sendVerificationEmailToUser(authResult.user)
                    } else dispatcher.dispatchOnUi(CreateAccountCompleteAction(user = null, task = taskFailure(fetchTask.exception)))
                }
            } else {
                dispatcher.dispatchOnUi(CreateAccountCompleteAction(user = null, task = taskFailure(fetchTask.exception)))
            }
        }
    }

    override fun sendVerificationEmail() {
        if (authInstance.currentUser == null) {
            dispatcher.dispatchOnUi(VerificationEmailSentAction(taskFailure(FirebaseUserNotFound())))
            return
        }
        sendVerificationEmailToUser(authInstance.currentUser!!)
    }

    override fun signOut() {
        authInstance.signOut()
    }

    override fun verifyUser() {
        if (authInstance.currentUser == null) {
            dispatcher.dispatchOnUi(VerifyUserEmailCompleteAction(taskFailure(FirebaseUserNotFound()),
                verified = false))
            return
        }

        authInstance.currentUser!!.reload() //send a verification email needs to have a recent user instance. We need to reload it to avoid errors
            .addOnCompleteListener { reloadTask ->
                //After the reload the currentUser can be null because it takes some time in be updated
                if (reloadTask.isSuccessful && authInstance.currentUser != null) {
                    val user = authInstance.currentUser!!
                    dispatcher.dispatchOnUi(VerifyUserEmailCompleteAction(task = taskSuccess(), verified = user.isEmailVerified))
                } else dispatcher.dispatchOnUi(VerifyUserEmailCompleteAction(task = taskFailure(reloadTask.exception)))
            }
    }

    private fun sendVerificationEmailToUser(user: FirebaseUser) {
        user.reload() //send a verification email needs to have a recent user instance. We need to reload it to avoid errors
            .addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    user.sendEmailVerification().addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) dispatcher.dispatchOnUi(VerificationEmailSentAction(taskSuccess()))
                        else dispatcher.dispatchOnUi(VerificationEmailSentAction(task = taskFailure(emailTask.exception)))
                    }
                } else dispatcher.dispatchOnUi(VerificationEmailSentAction(task = taskFailure(reloadTask.exception)))
            }
    }
}