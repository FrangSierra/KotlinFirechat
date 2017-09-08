package frangsierra.kotlinfirechat.common.flux

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import frangsierra.kotlinfirechat.BuildConfig
import frangsierra.kotlinfirechat.common.dagger.*
import frangsierra.kotlinfirechat.common.log.DebugTree
import frangsierra.kotlinfirechat.common.log.Grove
import frangsierra.kotlinfirechat.common.misc.collectDeviceBuildInformation
import gg.grizzlygrit.util.Prefs
import kotlin.properties.Delegates

private var _app: App by Delegates.notNull<App>()
private var _prefs: Prefs by Delegates.notNull<Prefs>()
val app: App get() = _app
val prefs: Prefs get() = _prefs

class App :
    Application(),
    ComponentManager by DefaultComponentManager() {

    val exceptionHandlers: MutableList<Thread.UncaughtExceptionHandler> = ArrayList()
    val component: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(app))
            .build()
    }
    override fun onCreate() {
        super.onCreate()
        _app = this
        _prefs = Prefs(this)
        if (BuildConfig.DEBUG) {
            Grove.plant(DebugTree(true))
            Grove.d { collectDeviceBuildInformation(this) }
        }

        registerComponent(object : ComponentFactory<AppComponent> {
            override fun createComponent(): AppComponent {
                return DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()
            }

            override val componentType = AppComponent::class
        })

        val appComponent = findComponent(AppComponent::class)
        val stores = appComponent.stores()
        initStores(stores.values.toList())

        registerSystemCallbacks(appComponent.dispatcher(), this)

        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        exceptionHandlers.add(exceptionHandler)
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            exceptionHandlers.forEach { it.uncaughtException(thread, error) }
        }

        configureLeakCanary()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        trimComponents(level)
    }

    private fun configureLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)
    }

}

