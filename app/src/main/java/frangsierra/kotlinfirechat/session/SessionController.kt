package frangsierra.kotlinfirechat.session

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import durdinapps.rxfirebase2.RxFirebaseAuth
import frangsierra.kotlinfirechat.chat.OnTokenRefreshedAction
import frangsierra.kotlinfirechat.common.dagger.AppComponent
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.app
import frangsierra.kotlinfirechat.common.log.Grove
import javax.inject.Inject

interface SessionController {

    /**
     * Check the providers associated to the given email and based on that, tries to create a new account or
     * login with the given data.
     */
    fun tryToLoginWithEmail(state: SessionState, email: String, password: String): SessionState

    /**
     * Sign out the current user.
     */
    fun signOut(state: SessionState): SessionState

    /**
     * Try to create a new account with an email and a password.
     */
    fun createAccount(state: SessionState, password: String, user: User): SessionState

    /**
     * Update the state with the right values after an account is correctly created.
     */
    fun onAccountSuccesfullyCreated(state: SessionState, createdUser: FirebaseUser): SessionState

    /**
     * Update the state with a new given status.
     */
    fun onSessionStatusChange(state: SessionState, loginStatus: LoginStatus, loggedUser: FirebaseUser?): SessionState

    /**
     * Update the state with an error when the authentication fail.
     */
    fun onAuthenticationError(state: SessionState, error: Throwable?): SessionState

    /**
     * Check the associated providers to the given credential to login or create an account with it.
     */
    fun tryToLoginWithCredential(state: SessionState, credential: AuthCredential, user: User): SessionState
}

@AppScope
class SessionControllerImpl @Inject constructor() : SessionController, FirebaseInstanceIdService() {
    //We should retrieve this data from the component cause the FirebaseInstanceIdService should
    //have an empty constructor.
    val dispatcher: Dispatcher = app.findComponent(AppComponent::class).dispatcher()
    private val authInstance = FirebaseAuth.getInstance()

    override fun tryToLoginWithCredential(state: SessionState, credential: AuthCredential, user: User): SessionState {
        var newAccount = false

        val dataDisposables = state.dataDisposables.apply {
            add(
                RxFirebaseAuth.fetchProvidersForEmail(authInstance, user.email!!)
                    .flatMap {
                        //If the given email not contains the current provider, it's a new account.
                        newAccount = !it.providers!!.contains(credential.provider)
                        return@flatMap RxFirebaseAuth.signInWithCredential(authInstance, credential)
                    }
                    .subscribe({ result ->
                        val action = if (newAccount) AccountSuccessfullyCreatedAction(result.user!!, user)
                        else AuthenticationStatusChangedAction(LoginStatus.LOGGED, result.user!!)
                        dispatcher.dispatchOnUi(action)
                    }) { throwable ->
                        dispatcher.dispatch(AuthenticationErrorAction(throwable))
                    })
        }
        return state.copy(loggedUser = null, status = LoginStatus.LOGGING, dataDisposables = dataDisposables)
    }

    override fun onSessionStatusChange(state: SessionState, loginStatus: LoginStatus, loggedUser: FirebaseUser?): SessionState =
        state.copy(status = loginStatus, loggedUser = loggedUser)

    override fun createAccount(state: SessionState, password: String, user: User): SessionState {
        val dataDisposables = state.dataDisposables.apply {
            add(
                RxFirebaseAuth.createUserWithEmailAndPassword(authInstance, user.email!!, password)
                    .subscribe({ authResult ->
                        dispatcher.dispatchOnUi(AccountSuccessfullyCreatedAction(authResult.user, user))
                    }) { dispatcher.dispatchOnUi(AuthenticationErrorAction(it)) })
        }
        return state.copy(status = LoginStatus.CREATING_ACCOUNT, dataDisposables = dataDisposables)
    }

    override fun onAccountSuccesfullyCreated(state: SessionState, createdUser: FirebaseUser): SessionState =
        state.copy(loggedUser = createdUser, status = LoginStatus.LOGGED)

    override fun onAuthenticationError(state: SessionState, error: Throwable?): SessionState =
        state.copy(loggedUser = null, status = LoginStatus.UNLOGGED, throwable = error)

    override fun tryToLoginWithEmail(state: SessionState, email: String, password: String): SessionState {
        val dataDisposables = state.dataDisposables.apply {
            add(
                RxFirebaseAuth.signInWithEmailAndPassword(authInstance, email, password)
                    .filter { authResult -> authResult.user != null }
                    .subscribe(
                        { authResult -> dispatcher.dispatchOnUi(AuthenticationStatusChangedAction(LoginStatus.LOGGED, authResult.user)) })
                    { dispatcher.dispatchOnUi(AuthenticationErrorAction(it)) })
        }
        return state.copy(loggedUser = null, status = LoginStatus.LOGGING, dataDisposables = dataDisposables)
    }

    override fun signOut(state: SessionState): SessionState =
        state.copy(loggedUser = null, status = LoginStatus.UNLOGGED)

    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Grove.d { "Refreshed token: $refreshedToken" }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        refreshedToken?.let { dispatcher.dispatchOnUi(OnTokenRefreshedAction(it)) }
    }
}
