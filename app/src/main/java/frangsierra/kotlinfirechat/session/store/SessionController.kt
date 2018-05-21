package frangsierra.kotlinfirechat.session.store

import com.google.firebase.auth.*
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.Dispatcher
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
     * Send a reset password email to the given user.
     *
     * @param email email of the user
     */
    fun sendResetPasswordEmail(email: String)

    /**
     * Send a verification email to the user logged in the auth instance.
     */
    fun sendVerificationEmail()

    /**
     * Refresh the instance of the [FirebaseUser] with all the updated data from the server.
     */
    fun refreshUser()

    /**
     * Try to link a new credential to a current instance of a [FirebaseUser].
     *
     * @param credential the [AuthCredential] with all the info of the provider
     * @param email The email of the [User]
     */
    fun linkCredentialToUser(credential: AuthCredential, email: String)


    /**
     * Unlink a provider from a [FirebaseUser] instance.
     *
     * @param provider Provider which is going to be unlinked
     */
    fun unlinkCredential(provider: LoginProvider)

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loginWithCredentials(email: String, password: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createAccountWithCredentials(email: String, password: String, username: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loginWithProviderCredentials(credential: AuthCredential, email: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createAccountWithProviderCredentials(credential: AuthCredential, userData: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendResetPasswordEmail(email: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendVerificationEmail() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refreshUser() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun linkCredentialToUser(credential: AuthCredential, email: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unlinkCredential(provider: LoginProvider) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun signOut() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun verifyUser() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}