package frangsierra.kotlinfirechat.common.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.app

@Module
class FirebaseModule {
    @Provides @AppScope
    fun provideAuthentication(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @AppScope
    fun provideDatabase(): FirebaseDatabase {
        val instance = FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) }
        return instance
    }

    @Provides @AppScope
    fun provideMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides @AppScope
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides @AppScope
    fun provideInstanceId(): FirebaseInstanceId = FirebaseInstanceId.getInstance()

    @Provides @AppScope
    fun providePerformance(): FirebasePerformance = FirebasePerformance.getInstance()

    @Provides @AppScope
    fun provideAnalytics(): FirebaseAnalytics = FirebaseAnalytics.getInstance(app)
}
