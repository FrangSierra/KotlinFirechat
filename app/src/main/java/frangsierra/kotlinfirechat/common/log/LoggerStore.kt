package frangsierra.kotlinfirechat.common.log

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.*
import frangsierra.kotlinfirechat.common.flux.OnActivityLifeCycleAction.ActivityStage.DESTROYED
import frangsierra.kotlinfirechat.common.flux.OnActivityLifeCycleAction.ActivityStage.STOPPED
import javax.inject.Inject


@AppScope
class LoggerStore @Inject constructor(
        val dispatcher: Dispatcher,
        context: Application,
        val lazyStoreMap: LazyStoreMap) : Store<LoggerState>() {

    private val fileLogController = FileLogController(context)

    override fun initialState() = LoggerState()

    override fun init() {
        val fileTree = fileLogController.newFileTree()
        if (fileTree != null) {
            Grove.plant(fileTree)
        }

        dispatcher.subscribe(OnActivityLifeCycleAction::class) {
            when (it.stage) {
                STOPPED, DESTROYED -> fileTree?.flush()
                else -> { //No-op
                }
            }
        }

        dispatcher.addInterceptor(LoggerInterceptor(lazyStoreMap.get().values))
        app.exceptionHandlers.add(Thread.UncaughtExceptionHandler { _, _ ->
            fileTree?.flush()
        })
    }
}

class LoggerState

@Module
abstract class LoggerModule {
    @Binds @AppScope @IntoMap @ClassKey(LoggerStore::class)
    abstract fun storeToMap(store: LoggerStore): Store<*>
}