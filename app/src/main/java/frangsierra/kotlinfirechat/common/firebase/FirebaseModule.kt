package frangsierra.kotlinfirechat.common.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import dagger.Module
import dagger.Provides
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.flux.app


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
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        firestoreSettings = settings
    }
}