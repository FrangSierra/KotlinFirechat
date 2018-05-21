package frangsierra.kotlinfirechat.session.store

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.Store
import javax.inject.Inject

@AppScope
class SessionStore @Inject constructor() : Store<SessionState>() {
    override fun init() {

    }
}

data class SessionState(val string: String)

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
