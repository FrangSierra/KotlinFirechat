package frangsierra.kotlinfirechat.session.store

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.core.dagger.AppScope
import mini.Reducer
import mini.Store
import javax.inject.Inject

@AppScope
class SessionStore @Inject constructor() : Store<SessionState>() {

    @Reducer
    fun loginWithCredentials(action: LoginWithCredentials, state : SessionState) : SessionState {
        return state
    }

    @Reducer
    fun loginComplete(action: LoginCompleteAction, state : SessionState) : SessionState {
    return state
    }

    @Reducer
    fun loginWithProvider(action: LoginWithProviderCredentials, state : SessionState) : SessionState {
        return state
    }
}

@Module
abstract class SessionModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(SessionStore::class)
    abstract fun provideSessionStore(store: SessionStore): Store<*>

    @Binds
    @AppScope
    abstract fun bindSessionController(impl: SessionControllerImpl): SessionController
}
