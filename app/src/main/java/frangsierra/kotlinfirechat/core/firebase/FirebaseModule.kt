package frangsierra.kotlinfirechat.core.firebase

import android.content.Context
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import frangsierra.kotlinfirechat.core.dagger.AppScope
import frangsierra.kotlinfirechat.core.flux.app


@Module
class FirebaseModule {
    @Provides
    @AppScope
    fun provideAuthentication(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @AppScope
    fun provideInstanceId(): FirebaseInstanceId = FirebaseInstanceId.getInstance()

    @Provides
    @AppScope
    fun provideAnalytics(): FirebaseAnalytics = FirebaseAnalytics.getInstance(app)

    @Provides
    @AppScope
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @AppScope
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .setPersistenceEnabled(true)
                .build()
        firestoreSettings = settings
    }

    @Provides @AppScope
    fun provideFirebaseDispatcher(context: Context): FirebaseJobDispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
}