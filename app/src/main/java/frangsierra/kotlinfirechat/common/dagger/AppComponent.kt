package frangsierra.kotlinfirechat.common.dagger

import android.app.Application
import android.content.Context
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import dagger.Component
import dagger.Module
import dagger.Provides
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.chat.ChatActivity
import frangsierra.kotlinfirechat.chat.ChatModule
import frangsierra.kotlinfirechat.common.firebase.FirebaseModule
import frangsierra.kotlinfirechat.common.flux.*
import frangsierra.kotlinfirechat.common.log.LoggerModule
import frangsierra.kotlinfirechat.session.CreateAccountActivity
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.session.SessionModule
import frangsierra.kotlinfirechat.user.UserStoreModule

@Component(modules = arrayOf(
    AppModule::class,
    LoggerModule::class,
    FirebaseModule::class,
    SessionModule::class,
    UserStoreModule::class,
    ChatModule::class
))
@AppScope
interface AppComponent : StoreHolderComponent {
    fun dispatcher(): Dispatcher

    fun mainActivityComponent(): AppComponent

    fun inject(into: LoginActivity)

    fun inject(into: CreateAccountActivity)

    fun inject(into: ChatActivity)
}

object AppComponentFactory : ComponentFactory<AppComponent> {
    override fun createComponent() =
        app.findComponent(AppComponent::class)
            .mainActivityComponent()
            .also { initStores(it.stores().values) }

    override fun destroyComponent(component: AppComponent) {
        disposeStores(component.stores().values)
    }

    override val componentType = AppComponent::class
}

@Module
class AppModule(val app: App) {
    @Provides @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app

    @Provides @AppScope
    fun provideGoogleApiClient(): GoogleApiClient {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            //Token is retrieved automatically from google.services
            .requestIdToken(app.getString(R.string.default_web_client_id))
            .requestEmail()
            .build().let {
            return GoogleApiClient.Builder(app)
                .addApi(Auth.GOOGLE_SIGN_IN_API, it)
                .build()
        }
    }
}