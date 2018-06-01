package frangsierra.kotlinfirechat.core.dagger

import android.app.Application
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import frangsierra.kotlinfirechat.core.firebase.FirebaseModule
import frangsierra.kotlinfirechat.core.flux.App
import frangsierra.kotlinfirechat.core.flux.FluxActivity
import frangsierra.kotlinfirechat.home.HomeActivity
import frangsierra.kotlinfirechat.session.CreateAccountActivity
import frangsierra.kotlinfirechat.session.EmailVerificationActivity
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.session.store.SessionModule
import mini.Dispatcher
import mini.StoreMap

interface AppComponent {
    fun dispatcher(): Dispatcher
    fun stores(): StoreMap
}

@AppScope
@Component(modules = [(AppModule::class),
    (FirebaseModule::class),
    (SessionModule::class)])

interface DefaultAppComponent : AppComponent {
    fun inject(target: FluxActivity)
    fun inject(target: LoginActivity)
    fun inject(target: CreateAccountActivity)
    fun inject(target: EmailVerificationActivity)
    fun inject(target: HomeActivity)
}

@Module
class AppModule(val app: App) {
    @Provides
    @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app
}