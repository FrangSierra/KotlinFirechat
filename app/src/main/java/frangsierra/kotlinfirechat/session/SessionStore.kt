package frangsierra.kotlinfirechat.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import durdinapps.rxfirebase2.RxFirebaseAuth
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.firebase.FirebaseMockModels
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.Store
import frangsierra.kotlinfirechat.messaging.TokenStatus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class SessionStore @Inject constructor(val dispatcher: Dispatcher,
                                       val authInstance: FirebaseAuth,
                                       val controller: SessionController) : Store<SessionState>() {
    override fun init() {

        if (!FirebaseMockModels.USE_FIREBASE_MOCK) {
            RxFirebaseAuth.observeAuthState(authInstance)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe({ firebaseAuth ->
                    val loginStatus = if (firebaseAuth.currentUser != null) LoginStatus.LOGGED else LoginStatus.UNLOGGED
                    dispatcher.dispatchOnUi(AuthenticationStatusChangedAction(loginStatus, firebaseAuth.currentUser))
                }, { throwable -> dispatcher.dispatchOnUi(AuthenticationErrorAction(throwable)) }).track()
        }
        dispatcher.subscribe(AuthenticationStatusChangedAction::class)
            .flowable()
            .filter { it.loginStatus != state.status }
            .subscribe {
                state = controller.onSessionStatusChange(state, it.loginStatus, it.loggedUser)
            }.track()

        dispatcher.subscribe(AuthenticationErrorAction::class) {
            state = controller.onAuthenticationError(state, it.throwable)
        }.track()

        dispatcher.subscribe(AccountSuccessfullyCreatedAction::class) {
            state = controller.onAccountSuccesfullyCreated(state, it.createdUser)
        }.track()

        dispatcher.subscribe(CreateAccountWithEmailAction::class) {
            state = controller.createAccount(state, it.password, it.user)
        }.track()

        dispatcher.subscribe(ManageCredentialAction::class) {
            state = controller.tryToLoginWithCredential(state, it.credential, it.user)
        }.track()

        dispatcher.subscribe(LoginWithEmailAndPasswordAction::class) {
            state = controller.tryToLoginWithEmail(state, it.email, it.password)
        }.track()

        dispatcher.subscribe(SignOutAction::class) {
            state.dataDisposables.clear()
            state = initialState()
            authInstance.signOut()
        }.track()
    }

    override fun cancelSubscriptions() {
        state.dataDisposables.clear()
        super.cancelSubscriptions()
    }
}

data class SessionState(val status: LoginStatus = LoginStatus.LOGGING,
                        val tokenStatus: TokenStatus = TokenStatus.NULL,
                        val loggedUser: FirebaseUser? = null, val throwable: Throwable? = null,
                        val dataDisposables: CompositeDisposable = CompositeDisposable())

@Module
class SessionModule {
    @Provides
    @AppScope
    @IntoMap
    @ClassKey(SessionStore::class)
    fun provideSessionStore(store: SessionStore): Store<*> = store

    @Provides
    @AppScope
    fun provideSessionController(impl: SessionControllerImpl,
                                 fake: SessionControllerFake): SessionController =
        if (FirebaseMockModels.USE_FIREBASE_MOCK) fake else impl

}

enum class LoginStatus {
    LOGGING,
    LOGGED,
    CREATING_ACCOUNT,
    UNLOGGED
}
