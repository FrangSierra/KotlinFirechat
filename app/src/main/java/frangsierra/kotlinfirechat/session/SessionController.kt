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
    fun tryToLoginWithEmail(state: SessionState, email: String, password: String): SessionState

    fun signOut(state: SessionState): SessionState

    fun createAccount(state: SessionState, password: String, user: User): SessionState

    fun onAccountSuccesfullyCreated(state: SessionState, createdUser: FirebaseUser): SessionState

    fun onSessionStatusChange(state: SessionState, loginStatus: LoginStatus, loggedUser: FirebaseUser?): SessionState

    fun onAuthenticationError(state: SessionState, error: Throwable?): SessionState

    fun tryToLoginWithCredential(state: SessionState, credential: AuthCredential, user: User): SessionState
}

@AppScope
class SessionControllerImpl @Inject constructor() : SessionController, FirebaseInstanceIdService() {
    val dispatcher: Dispatcher = app.findComponent(AppComponent::class).dispatcher()
    val authInstance = FirebaseAuth.getInstance()

    override fun tryToLoginWithCredential(state: SessionState, credential: AuthCredential, user: User): SessionState {
        val dataDisposables = state.dataDisposables
        var newAccount: Boolean = false

        dataDisposables.add(RxFirebaseAuth.fetchProvidersForEmail(authInstance, user.email!!)
            .flatMap {
                newAccount = !it.providers!!.contains(credential.provider)
                return@flatMap RxFirebaseAuth.signInWithCredential(authInstance, credential)
            }
            .subscribe({
                val action = if (newAccount) AccountSuccessfullyCreatedAction(it.user!!, user)
                else AuthenticationStatusChangedAction(LoginStatus.LOGGED, it.user!!)
                dispatcher.dispatchOnUi(action)
            }) { throwable -> dispatcher.dispatch(AuthenticationErrorAction(throwable)) })

        return state.copy(loggedUser = null, status = LoginStatus.LOGGING, dataDisposables = dataDisposables)
    }

    override fun onSessionStatusChange(state: SessionState, loginStatus: LoginStatus, loggedUser: FirebaseUser?): SessionState {

        return state.copy(status = loginStatus, loggedUser = loggedUser)
    }

    override fun createAccount(state: SessionState, password: String, user: User): SessionState {
        val dataDisposables = state.dataDisposables
        dataDisposables.add(RxFirebaseAuth.createUserWithEmailAndPassword(authInstance, user.email!!, password)
            .subscribe({ authResult -> dispatcher.dispatchOnUi(AccountSuccessfullyCreatedAction(authResult.user, user)) }
            ) { dispatcher.dispatchOnUi(AuthenticationErrorAction(it)) })
        return state.copy(status = LoginStatus.CREATING_ACCOUNT, dataDisposables = dataDisposables)
    }

    override fun onAccountSuccesfullyCreated(state: SessionState, createdUser: FirebaseUser): SessionState {
        return state.copy(loggedUser = createdUser, status = LoginStatus.LOGGED)
    }

    override fun onAuthenticationError(state: SessionState, error: Throwable?): SessionState {
        return state.copy(loggedUser = null, status = LoginStatus.UNLOGGED, throwable = error)
    }

    override fun tryToLoginWithEmail(state: SessionState, email: String, password: String): SessionState {
        val dataDisposables = state.dataDisposables
        dataDisposables.add(RxFirebaseAuth.signInWithEmailAndPassword(authInstance, email, password)
            .filter { authResult -> authResult.user != null }
            .subscribe(
                { authResult -> dispatcher.dispatchOnUi(AuthenticationStatusChangedAction(LoginStatus.LOGGED, authResult.user)) })
            { dispatcher.dispatchOnUi(AuthenticationErrorAction(it)) })
        return state.copy(loggedUser = null, status = LoginStatus.LOGGING, dataDisposables = dataDisposables)
    }

    override fun signOut(state: SessionState): SessionState {
        return state.copy(loggedUser = null, status = LoginStatus.UNLOGGED)
    }

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
