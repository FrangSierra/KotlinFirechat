package frangsierra.kotlinfirechat.session.controller

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.*
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.core.flux.doAsync
import frangsierra.kotlinfirechat.session.model.*
import frangsierra.kotlinfirechat.session.store.CreateAccountCompleteAction
import frangsierra.kotlinfirechat.session.store.LoginCompleteAction
import frangsierra.kotlinfirechat.session.store.VerificationEmailSentAction
import frangsierra.kotlinfirechat.session.store.VerifyUserEmailCompleteAction
import mini.Dispatcher
import mini.taskFailure
import mini.taskSuccess
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
            doAsync {
                if (firebaseAuth.currentUser == null || !firebaseAuth.currentUser!!.isEmailVerified)
                    dispatcher.dispatch(LoginCompleteAction(task = taskFailure()))
                else {
                    val currentUser = firebaseAuth.currentUser!!
                    dispatcher.dispatch(LoginCompleteAction(task = taskSuccess(),
                        user = currentUser.toUser(),
                        associatedProviders = currentUser.associatedProviders()))

                }
            }
        }
    }

    override fun loginWithCredentials(email: String, password: String) {
        doAsync {
            try {
                val result = Tasks.await(authInstance.signInWithEmailAndPassword(email, password))
                val emailVerified = result.user.isEmailVerified
                val user = result.user.toUser()
                val providers = result.user.associatedProviders()
                dispatcher.dispatch(LoginCompleteAction(
                    user = user,
                    emailVerified = emailVerified,
                    task = taskSuccess(),
                    associatedProviders = providers))
            } catch (e: Throwable) {
                dispatcher.dispatch(LoginCompleteAction(user = null, task = taskFailure(e)))
            }
        }
    }

    override fun createAccountWithCredentials(email: String, password: String, username: String) {
        doAsync {
            try {
                val result = Tasks.await(authInstance.createUserWithEmailAndPassword(email, password))
                val firebaseUser = result.user
                val emailVerified = firebaseUser.isEmailVerified
                val user = firebaseUser.toUser().copy(username = username)
                val providers = firebaseUser.associatedProviders()
                dispatcher.dispatch(CreateAccountCompleteAction(
                    user = user,
                    emailVerified = emailVerified,
                    task = taskSuccess(),
                    associatedProviders = providers))
                if (!emailVerified) sendVerificationEmailToUser(firebaseUser)
            } catch (e: Throwable) {
                dispatcher.dispatch(LoginCompleteAction(user = null, task = taskFailure(e)))
            }
        }
    }

    override fun loginWithProviderCredentials(credential: AuthCredential, email: String) {
        doAsync {
            try {
                val providerResults = Tasks.await(authInstance.fetchSignInMethodsForEmail(email))
                val isNewAccount = providerResults.signInMethods!!.contains((credential.provider))
                if (isNewAccount) {
                    val authResult = Tasks.await(authInstance.signInWithCredential(credential))
                    val emailVerified = authResult.user.isEmailVerified
                    val user = authResult.user.toUser()
                    val providers = authResult.user.associatedProviders()
                    dispatcher.dispatch(LoginCompleteAction(
                        user = user,
                        emailVerified = emailVerified,
                        task = taskSuccess(),
                        associatedProviders = providers))
                    if (!emailVerified) sendVerificationEmailToUser(authResult.user)
                } else {
                    dispatcher.dispatch(LoginCompleteAction(user = null, task = taskFailure(ProviderNotLinkedException(credential.provider))))
                }
            } catch (e: Throwable) {
                dispatcher.dispatch(LoginCompleteAction(user = null, task = taskFailure(e)))
            }
        }
    }

    override fun createAccountWithProviderCredentials(credential: AuthCredential, userData: User) {
        doAsync {
            try {
                val providerResults = Tasks.await(authInstance.fetchSignInMethodsForEmail(userData.email))
                val isNewAccount = providerResults.signInMethods!!.contains((credential.provider))
                if (isNewAccount) {
                    val authResult = Tasks.await(authInstance.signInWithCredential(credential))
                    val emailVerified = authResult.user.isEmailVerified
                    val user = authResult.user.toUser().copy(photoUrl = userData.photoUrl)
                    val providers = authResult.user.associatedProviders()
                    dispatcher.dispatch(CreateAccountCompleteAction(
                        user = user,
                        alreadyExisted = !isNewAccount,
                        emailVerified = emailVerified,
                        task = taskSuccess(),
                        associatedProviders = providers))
                    if (!emailVerified) sendVerificationEmailToUser(authResult.user)
                } else {
                    dispatcher.dispatch(CreateAccountCompleteAction(user = null, task = taskFailure(ProviderNotLinkedException(credential.provider))))
                }
            } catch (e: Throwable) {
                dispatcher.dispatch(CreateAccountCompleteAction(user = null, task = taskFailure(e)))
            }
        }
    }

    override fun sendVerificationEmail() {
        if (authInstance.currentUser == null) {
            dispatcher.dispatch(VerificationEmailSentAction(taskFailure(FirebaseUserNotFound())))
            return
        }
        sendVerificationEmailToUser(authInstance.currentUser!!)
    }

    override fun signOut() {
        authInstance.signOut()
    }

    override fun verifyUser() {
        if (authInstance.currentUser == null) {
            dispatcher.dispatch(VerifyUserEmailCompleteAction(taskFailure(FirebaseUserNotFound()),
                verified = false))
            return
        }

        authInstance.currentUser!!.reload() //send a verification email needs to have a recent user instance. We need to reload it to avoid errors
            .addOnCompleteListener { reloadTask ->
                //After the reload the currentUser can be null because it takes some time in be updated
                if (reloadTask.isSuccessful && authInstance.currentUser != null) {
                    val user = authInstance.currentUser!!
                    dispatcher.dispatch(VerifyUserEmailCompleteAction(task = taskSuccess(), verified = user.isEmailVerified))
                } else dispatcher.dispatch(VerifyUserEmailCompleteAction(task = taskFailure(reloadTask.exception)))
            }
    }

    private fun sendVerificationEmailToUser(user: FirebaseUser) {
        doAsync {
            try {
                Tasks.await(user.reload())
                Tasks.await(user.sendEmailVerification())
                dispatcher.dispatch(VerificationEmailSentAction(taskSuccess()))
            } catch (e: Throwable) {
                dispatcher.dispatch(VerificationEmailSentAction(task = taskFailure(e)))
            }
        }
    }
}