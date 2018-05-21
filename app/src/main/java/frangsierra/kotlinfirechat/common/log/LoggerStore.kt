package frangsierra.kotlinfirechat.common.log

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.LazyStoreMap
import frangsierra.kotlinfirechat.common.flux.Store
import javax.inject.Inject


@AppScope
class LoggerStore @Inject constructor(val lazyStoreMap: LazyStoreMap) : Store<LoggerState>() {

    override fun initialState() = LoggerState()
    override fun init() {
        dispatcher.addInterceptor(LoggerInterceptor(lazyStoreMap.get().values))
    }
}

class LoggerState


@Module
abstract class LoggerModule {
    @Binds
    @IntoMap
    @ClassKey(LoggerStore::class)
    abstract fun provideLoggerStoreToMap(store: LoggerStore): Store<*>
}