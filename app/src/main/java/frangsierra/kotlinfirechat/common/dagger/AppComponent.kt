package frangsierra.kotlinfirechat.common.dagger

import android.app.Application
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import frangsierra.kotlinfirechat.common.firebase.FirebaseModule
import frangsierra.kotlinfirechat.common.flux.App
import frangsierra.kotlinfirechat.common.flux.Dispatcher
import frangsierra.kotlinfirechat.common.flux.FluxActivity
import frangsierra.kotlinfirechat.common.log.LoggerModule
import frangsierra.kotlinfirechat.session.CreateAccountActivity
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.session.store.SessionModule
import frangsierra.kotlinfirechat.common.flux.StoreMap

interface AppComponent {
    fun dispatcher(): Dispatcher
    fun stores(): StoreMap
}

@AppScope
@Component(modules = [(AppModule::class),
    (LoggerModule::class),
    (FirebaseModule::class),
    (SessionModule::class)])

interface DefaultAppComponent : AppComponent {
    fun inject(target: FluxActivity)
    fun inject(target: LoginActivity)
    fun inject(target: CreateAccountActivity)
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