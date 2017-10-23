package frangsierra.kotlinfirechat.session

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import frangsierra.kotlinfirechat.common.firebase.FirebaseMockModels
import frangsierra.kotlinfirechat.common.firebase.User
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.app
import javax.inject.Inject

class SessionControllerFake @Inject constructor() : SessionController {
       private val dispatcher: Dispatcher = app.component.dispatcher()

    override fun tryToLoginWithEmail(state: SessionState, email: String, password: String): SessionState {
        dispatcher.dispatchOnUi(AuthenticationStatusChangedAction(LoginStatus.LOGGED,
            FirebaseMockModels.mockFirebaseUser))
        return state.copy(loggedUser = null, status = LoginStatus.LOGGING)
    }

    override fun signOut(state: SessionState): SessionState {
        return state.copy(loggedUser = null, status = LoginStatus.UNLOGGED)
    }

    override fun createAccount(state: SessionState, password: String, user: User): SessionState {
        dispatcher.dispatchOnUi(AccountSuccessfullyCreatedAction(FirebaseMockModels.mockFirebaseUser, FirebaseMockModels.mockUser))
        return state.copy(status = LoginStatus.CREATING_ACCOUNT)
    }

    override fun onAccountSuccesfullyCreated(state: SessionState, createdUser: FirebaseUser): SessionState {
        return state.copy(loggedUser = createdUser, status = LoginStatus.LOGGED)
    }

    override fun onSessionStatusChange(state: SessionState, loginStatus: LoginStatus, loggedUser: FirebaseUser?): SessionState {
        return state.copy(status = loginStatus, loggedUser = loggedUser)
    }

    override fun onAuthenticationError(state: SessionState, error: Throwable?): SessionState {
        return state.copy(loggedUser = null, status = LoginStatus.UNLOGGED, throwable = error)
    }

    override fun tryToLoginWithCredential(state: SessionState, credential: AuthCredential, user: User): SessionState {
        dispatcher.dispatchOnUi(AuthenticationStatusChangedAction(LoginStatus.LOGGED,
            FirebaseMockModels.mockFirebaseUser))
        return state.copy(loggedUser = null, status = LoginStatus.LOGGING)
    }

}
