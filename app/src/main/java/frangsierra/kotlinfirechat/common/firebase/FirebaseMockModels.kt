package frangsierra.kotlinfirechat.common.firebase

import frangsierra.kotlinfirechat.BuildConfig
import java.util.*

class FirebaseMockModels {

    companion object {
        val USE_FIREBASE_MOCK = false && BuildConfig.DEBUG

        val mockMessageKey get() = Random().nextLong().toString()
        val mockMessage = Message("Test", "User Test")
        val mockUser = User("Test", "User", "tests@testing.com")
        val mockFirebaseUser = MockFirebaseUser("testUid", mockUser.email, mockUser.userName)

        val mockMessageRetrieved get() = mockMessageKey to mockMessage
    }

}
