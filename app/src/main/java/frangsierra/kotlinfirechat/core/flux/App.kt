package frangsierra.kotlinfirechat.core.flux

import android.app.Application
import com.crashlytics.android.Crashlytics
import frangsierra.kotlinfirechat.BuildConfig
import frangsierra.kotlinfirechat.core.dagger.AppComponent
import frangsierra.kotlinfirechat.core.errors.CrashlyticsHandler
import frangsierra.kotlinfirechat.util.Prefs
import io.fabric.sdk.android.Fabric
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

        //Initialize Fabric before add the custom UncaughtExceptionHandler!
        val fabric = Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(BuildConfig.DEBUG) // Enables Crashlytics debugger
                .build()
        Fabric.with(fabric)

        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CrashlyticsHandler(defaultUncaughtExceptionHandler))
    }
}