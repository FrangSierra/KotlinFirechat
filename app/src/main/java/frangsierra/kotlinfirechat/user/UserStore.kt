package frangsierra.kotlinfirechat.user

import com.google.firebase.iid.FirebaseInstanceId
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.chat.OnTokenRefreshedAction
import frangsierra.kotlinfirechat.chat.OnUserLoggedAction
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.Store
import frangsierra.kotlinfirechat.session.AccountSuccessfullyCreatedAction
import frangsierra.kotlinfirechat.session.LoginStatus
import frangsierra.kotlinfirechat.session.SessionStore
import javax.inject.Inject

@AppScope
class UserDatabaseStore @Inject constructor(val dispatcher: Dispatcher, val userController: UserDatabaseController,
                                            val sessionStore: SessionStore, val instanceId: FirebaseInstanceId) : Store<UserDatabaseState>() {
    override fun init() {

        sessionStore.flowable()
            .filter { (status) -> status == LoginStatus.LOGGED }
            .subscribe {
                dispatcher.dispatchOnUi(OnUserLoggedAction(it.loggedUser!!))
            }.track()

        dispatcher.subscribe(OnTokenRefreshedAction::class) {
            state = userController.onTokenRefreshedAction(state, it.refreshedToken)
        }.track()

        dispatcher.subscribe(OnUserLoggedAction::class) {
            state = userController.onUserLogged(state, it.loggedUser.uid, instanceId)
        }.track()

        dispatcher.subscribe(AccountSuccessfullyCreatedAction::class) {
            state = userController.createProfileData(state, it.createdUser, it.user)
        }.track()

    }
}

@Module
class UserStoreModule {
    @Provides
    @AppScope
    @IntoMap
    @ClassKey(UserDatabaseStore::class)
    fun provideUserDatabaseStore(store: UserDatabaseStore): Store<*> = store

    @Provides
    @AppScope
    fun provideUserDatabaseController(impl: UserDatabaseControllerImpl): UserDatabaseController = impl
}
