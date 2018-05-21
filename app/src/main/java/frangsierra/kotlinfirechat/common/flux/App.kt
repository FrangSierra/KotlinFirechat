package frangsierra.kotlinfirechat.common.flux

import android.app.Application
import frangsierra.kotlinfirechat.BuildConfig
import frangsierra.kotlinfirechat.common.dagger.*
import frangsierra.kotlinfirechat.common.log.DebugTree
import frangsierra.kotlinfirechat.common.log.Grove
import frangsierra.kotlinfirechat.common.misc.collectDeviceBuildInformation
import frangsierra.kotlinfirechat.util.Prefs
import org.jetbrains.annotations.TestOnly
import kotlin.properties.Delegates

private var _app: App? = null

private var _prefs: Prefs by Delegates.notNull()
private var _appComponent: AppComponent? = null
val app: App get() = _app!!
val prefs: Prefs get() = _prefs
val appComponent: AppComponent get() = _appComponent!!

@TestOnly
fun setAppComponent(component: AppComponent) {
    _appComponent = component
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        _app = this
        _prefs = Prefs(this)

        if (BuildConfig.DEBUG) Grove.plant(DebugTree(true))


        if (_appComponent == null) {
            _appComponent = DaggerDefaultAppComponent
                    .builder()
                    .appModule(AppModule(this))
                    .build()
            initStores(appComponent.stores().values.toList())
            registerSystemCallbacks(appComponent.dispatcher(), this)
        }
        Grove.d { collectDeviceBuildInformation(this) }
    }
}